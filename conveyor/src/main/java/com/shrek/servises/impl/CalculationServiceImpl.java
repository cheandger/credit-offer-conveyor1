package com.shrek.servises.impl;


import com.shrek.validators.ScoringDataDTOValidator;
import com.shrek.exceptions.ParametersValidationException;
import com.shrek.model.CreditDTO;
import com.shrek.model.EmploymentDTO;
import com.shrek.model.PaymentScheduleElement;
import com.shrek.model.ScoringDataDTO;
import com.shrek.servises.CalculationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.shrek.model.EmploymentDTO.EmploymentStatusEnum.*;
import static com.shrek.model.EmploymentDTO.PositionEnum.*;
import static com.shrek.model.ScoringDataDTO.GenderEnum.*;
import static com.shrek.model.ScoringDataDTO.MaritalStatusEnum.*;
import static java.time.Duration.between;

@Service
@PropertySource("classpath:config_properties.properties")

public class CalculationServiceImpl implements CalculationService {

    private static final Logger log = LoggerFactory.getLogger(CalculationServiceImpl.class);

    @Value("${BASE_RATE}")
    private BigDecimal BASE_RATE;
    @Value("${IS_INSURANCE_RATE}")
    private BigDecimal IS_INSURANCE_RATE;


    @Override

    public CreditDTO calculation(ScoringDataDTO scoringDataDTO) throws ParametersValidationException {

        DataBinder dataBinder = new DataBinder(scoringDataDTO);
        dataBinder.addValidators(new ScoringDataDTOValidator());
        dataBinder.validate();
        if (dataBinder.getBindingResult().hasErrors()) {
            ObjectError objectError = dataBinder.getBindingResult().getAllErrors().get(0);
            throw new ParametersValidationException(objectError.getDefaultMessage());

        }

        log.info("Расчет кредитного продукта");
        BigDecimal finalTotalAmount = IS_INSURANCETotalAmountCalculation(scoringDataDTO);

        log.info("Перерасчет ,базовой ставки с учетом условий скорринга");
        BigDecimal finalRate = evaluationRateForScoring(scoringDataDTO).setScale(2, RoundingMode.HALF_UP);

        log.info("Расчет сотой доли месячной ставки");
        BigDecimal aHundredthPartOfMonthlyRate = finalRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP).setScale(8, RoundingMode.HALF_UP);
        log.info("Сотая доля месячной ставки составляет  " + aHundredthPartOfMonthlyRate);

        log.info("Расчет ежемесячного платежа ");
        BigDecimal monthlyPayment = finalTotalAmount.multiply((aHundredthPartOfMonthlyRate
                .add((aHundredthPartOfMonthlyRate
                        .divide(((BigDecimal.valueOf(1)
                                .add(aHundredthPartOfMonthlyRate))
                                .pow(scoringDataDTO.getTerm()))
                                .subtract(BigDecimal.valueOf(1)), 8, RoundingMode.HALF_UP))))).setScale(2, RoundingMode.HALF_UP);

        log.info("Ежемесячный платеж с учетом ануитетного графика погашения составляет " + monthlyPayment);


        BigDecimal firstMonthInterestPayment = finalTotalAmount.multiply(aHundredthPartOfMonthlyRate).setScale(2, RoundingMode.HALF_UP);
        log.info("Расчет платежа по процентам кредита за первый месяц:  " + firstMonthInterestPayment);

        BigDecimal firstMonthDeptPayment = monthlyPayment.subtract(firstMonthInterestPayment).setScale(2, RoundingMode.HALF_UP);
        log.info("Расчет платежа по телу кредита за первый месяц:  " + firstMonthDeptPayment);

        List<PaymentScheduleElement> scheduleList = new ArrayList<>();

        log.info("Составление первого элемента списка ежемесячных платежей");
        PaymentScheduleElement firstMonth = new PaymentScheduleElement()
                .number(1)
                .date(LocalDate.now().plusMonths(1))
                .totalPayment(monthlyPayment.setScale(2, RoundingMode.HALF_UP))//todo is it the summ of interestP & DeptP
                .interestPayment(firstMonthInterestPayment)
                .debtPayment(firstMonthDeptPayment)
                .remainingDebt(finalTotalAmount.subtract(firstMonthDeptPayment));

        log.info("Составление графика ежемесячных платежей");
        for (Integer i = 0; i < scoringDataDTO.getTerm(); i++) { // TODO: 07.01.2023   Made a List of payments
            if (i == 0) {
                scheduleList.add(firstMonth);
            } else {
                scheduleList.add(new PaymentScheduleElement()
                        .number(i + 1)
                        .date(firstMonth.getDate().plusMonths(1 + i))
                        .totalPayment(monthlyPayment)//
                        .interestPayment((scheduleList.get(i - 1).getRemainingDebt().multiply(aHundredthPartOfMonthlyRate)).setScale(2, RoundingMode.HALF_UP))
                        .debtPayment((monthlyPayment.subtract(scheduleList.get(i - 1).getRemainingDebt().multiply(aHundredthPartOfMonthlyRate))).setScale(2, RoundingMode.HALF_UP))
                        .remainingDebt((scheduleList.get(i - 1).getRemainingDebt().subtract((monthlyPayment.subtract(scheduleList.get(i - 1).getRemainingDebt().multiply(aHundredthPartOfMonthlyRate)))).setScale(2, RoundingMode.HALF_UP)))
                );

            }
        }
        log.info("Проверка и перераспределение вероятного остатка, вызванного округлением");

        if ((scheduleList.get(scheduleList.size() - 1).getRemainingDebt()).compareTo(BigDecimal.valueOf(0)) > 0 ||
                (scheduleList.get(scheduleList.size() - 1).getRemainingDebt()).compareTo(BigDecimal.valueOf(0)) < 0) {

            scheduleList.get(scheduleList.size() - 1).debtPayment(// If in the end of payment story the RemainingDebt doesn't 0(zero)(the problems of math rounding)
                    scheduleList.get(scheduleList.size() - 1).getDebtPayment()//we should add it to a debt payment and in to a totalPayment for the last month
                            .add(scheduleList.get(scheduleList.size() - 1).getRemainingDebt()));
            scheduleList.get(scheduleList.size() - 1).totalPayment(
                    scheduleList.get(scheduleList.size() - 1).getTotalPayment()
                            .add(scheduleList.get(scheduleList.size() - 1).getRemainingDebt()));

            scheduleList.get(scheduleList.size() - 1).remainingDebt(BigDecimal.valueOf(0));
        }
        log.info("График ежемесячных платежей  " + scheduleList);

        BigDecimal psk = pskCalculation(finalTotalAmount, scheduleList);

        log.info("""

                Кредитный продукт создан\s
                //////////////////////////////////////////////////////////////////////////////////////////////
                \s"""
        );

        return new CreditDTO().amount(scoringDataDTO.getAmount())
                .term(scoringDataDTO.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(finalRate)
                .psk(psk)
                .isInsuranceEnabled(scoringDataDTO.getIsInsuranceEnabled())
                .isSalaryClient(scoringDataDTO.getIsSalaryClient())
                .paymentSchedule(scheduleList);


    }

    public BigDecimal IS_INSURANCETotalAmountCalculation(ScoringDataDTO scoringDataDTO) {
        BigDecimal totalAmount = scoringDataDTO.getAmount().setScale(2, RoundingMode.HALF_UP);
        log.info("Перерасчет тела кредита с учетом условий скорринга");
        if (scoringDataDTO.getIsInsuranceEnabled()) {

            totalAmount = (totalAmount.add((IS_INSURANCE_RATE.multiply(totalAmount)
                    .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP))
                    .divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(scoringDataDTO.getTerm())))).setScale(2, RoundingMode.HALF_UP);
        }
        log.info("Тело кредита с учетом условий скорринга составляет  " + totalAmount);
        return totalAmount;
    }

    BigDecimal pskCalculation(BigDecimal amount, List<PaymentScheduleElement> paymentScheduleElementList) {
        log.info("Расчет ПСК");

        Integer size = paymentScheduleElementList.size() + 1;

        Double basePeriod = 30d;
        Double basePeriodYear = Math.floor(365 / basePeriod);

        LocalDate[] dates = new LocalDate[size];
        Double[] sum = new Double[size];
        Long[] days = new Long[size];
        Double[] e = new Double[size];
        Double[] q = new Double[size];

        dates[0] = paymentScheduleElementList.get(0).getDate().minusMonths(1);
        sum[0] = -amount.doubleValue();
        for (Integer n = 1; n < size; n++) {
            dates[n] = paymentScheduleElementList.get(n - 1).getDate();
            sum[n] = paymentScheduleElementList.get(n - 1).getTotalPayment().doubleValue();
        }
        for (Integer l = 0; l < size; l++) {
            days[l] = between(dates[0].atStartOfDay(), dates[l].atStartOfDay()).toDays();
            e[l] = ((days[l] % basePeriod) / basePeriod);
            q[l] = Math.floor(days[l] / basePeriod);
        }

        log.info("Расчет i");

        Double i = (double) 0;
        Double x = 1.0;
        Double s = 0.0001;
        while (x > 0) {
            x = (double) 0;
            for (Integer k = 0; k < size; k++) {
                x = x + sum[k] / ((1 + e[k] * i) * Math.pow(1 + i, q[k]));
            }
            i = i + s;
        }

        log.info("Расчет i окончен " + i);

        BigDecimal psk = BigDecimal.valueOf(i * basePeriodYear * 100).setScale(2, RoundingMode.HALF_UP);
        log.info("Рассчитаная ПСК составляет: " + psk + "% ");
        return psk;

    }


    public BigDecimal evaluationRateForScoring(ScoringDataDTO scoringDataDTO) {
        log.info("Расчет годовой ставки с учетом условий скорринга");

        BigDecimal preEvalRate = BASE_RATE;

        EmploymentDTO employmentDTO = scoringDataDTO.getEmployment();


        switch (employmentDTO.getEmploymentStatus()) {
            case SELF_EMPLOYED -> {
                if (scoringDataDTO.getEmployment().getEmploymentStatus().equals(SELF_EMPLOYED)){
                    preEvalRate = preEvalRate.add(BigDecimal.valueOf(1));}
            }

            case BUSINESS_OWNER -> {
                if (scoringDataDTO.getEmployment().getEmploymentStatus().equals(BUSINESS_OWNER)){
                    preEvalRate = preEvalRate.add(BigDecimal.valueOf(3));}
            }
        }
        switch (employmentDTO.getPosition()) {
            case MID_MANAGER -> {
                if (scoringDataDTO.getEmployment().getPosition().equals(MID_MANAGER)) {
                    preEvalRate = preEvalRate.subtract(BigDecimal.valueOf(2));
                }
            }
            case TOP_MANAGER -> {
                if (scoringDataDTO.getEmployment().getPosition().equals(TOP_MANAGER)) {
                    preEvalRate = preEvalRate.subtract(BigDecimal.valueOf(4));
                }
            }
        }
        if (scoringDataDTO.getIsInsuranceEnabled()) {
            preEvalRate = preEvalRate.subtract(BigDecimal.valueOf(3));
        }
        if (scoringDataDTO.getIsSalaryClient()) {
            preEvalRate = preEvalRate.subtract(BigDecimal.valueOf(1));
        }
        switch (scoringDataDTO.getMaritalStatus()) {
            case MARRIED -> {
                if (scoringDataDTO.getMaritalStatus().equals(MARRIED))
                    preEvalRate = preEvalRate.subtract(BigDecimal.valueOf(3));
            }
            case DIVORCED -> {
                if (scoringDataDTO.getMaritalStatus().equals(DIVORCED)){
                    preEvalRate = preEvalRate.add(BigDecimal.valueOf(1));}
            }
        }

        if (scoringDataDTO.getDependentAmount() > 1){
            preEvalRate = preEvalRate.add(BigDecimal.valueOf(1));}

        switch (scoringDataDTO.getGender()) {
            case MALE:
                if (scoringDataDTO.getGender().equals(MALE)&&(LocalDate.now().getYear() - scoringDataDTO.getBirthdate().getYear()) < 55 &&
                        (LocalDate.now().getYear() - scoringDataDTO.getBirthdate().getYear()) > 30) {
                    preEvalRate = preEvalRate.subtract(BigDecimal.valueOf(3));
                }

            case FEMALE:
                if (scoringDataDTO.getGender().equals(FEMALE)&&(LocalDate.now().getYear() - scoringDataDTO.getBirthdate().getYear()) < 60 &&
                        ( (LocalDate.now().getYear() - scoringDataDTO.getBirthdate().getYear()) > 35)) {
                    preEvalRate = preEvalRate.subtract(BigDecimal.valueOf(3));
                }
            case NON_BINARY:
                if (scoringDataDTO.getGender().equals(NON_BINARY)) {
                    preEvalRate = preEvalRate.add(BigDecimal.valueOf(3));
                }
                log.info("Годовая ставка покредиту составляет  " + preEvalRate);
                return preEvalRate;
        }
        return preEvalRate;
    }
}


 /*   credDTO
            "amount": "BigDecimal", передадим
            "term": "Integer",      передадим
          "monthlyPayment": "BigDecimal", посчитали
          "rate": "BigDecimal",  посчитали
          "psk": "BigDecimal",   посчитали
          "isInsuranceEnabled": "Boolean", предадим
          "isSalaryClient": "Boolean", передадим
          "paymentSchedule": "List<PaymentScheduleElement>"todo//needed to calc

*/