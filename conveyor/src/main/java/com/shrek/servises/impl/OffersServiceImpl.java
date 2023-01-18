package com.shrek.servises.impl;
import com.shrek.model.LoanApplicationRequestDTO;
import com.shrek.model.LoanOfferDTO;
import com.shrek.servises.OffersService;
import com.shrek.validators.LoanApplicationRequestDTOValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;
import com.shrek.exceptions.ParametersValidationException;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
@Service

public class OffersServiceImpl implements OffersService {
   @Value("${BASE_RATE}")
    private BigDecimal BASE_RATE;
    @Value("${IS_INSURANCE_RATE}")
    private BigDecimal IS_INSURANCE_RATE;
    private static final Logger log = LoggerFactory.getLogger(CalculationServiceImpl.class);

    @Override
    public List<LoanOfferDTO> offers(LoanApplicationRequestDTO loanApplicationRequestDTO) {

        DataBinder dataBinder = new DataBinder(loanApplicationRequestDTO);
        dataBinder.addValidators(new LoanApplicationRequestDTOValidator());
        dataBinder.validate();
        if (dataBinder.getBindingResult().hasErrors()) {
            ObjectError objectError = dataBinder.getBindingResult().getAllErrors().get(0);
            log.info("Проверка валидности входных данных");
            throw new ParametersValidationException(objectError.getDefaultMessage());
        }

        List<LoanOfferDTO> loanOfferDTOList=new ArrayList<>();
        log.info("Создание вариантов предварительных кредитных предложений");
        loanOfferDTOList.add(createLoanOffer(loanApplicationRequestDTO, false, false));
        loanOfferDTOList.add(createLoanOffer(loanApplicationRequestDTO, false, true));
        loanOfferDTOList.add(createLoanOffer(loanApplicationRequestDTO, true, false));
        loanOfferDTOList.add(createLoanOffer(loanApplicationRequestDTO, true, true));

        log.info("Формирование списка возможных вариантов кредитных предложений завершено");

        Comparator<LoanOfferDTO> rateComparator = Comparator.comparing(LoanOfferDTO::getRate);
        loanOfferDTOList.sort(Collections.reverseOrder(rateComparator));

        log.info("Список возможных вариантов кредитных предложений " + loanOfferDTOList);
        return loanOfferDTOList;

    }




    @Override
    public LoanOfferDTO createLoanOffer(@NotNull LoanApplicationRequestDTO loanApplicationRequestDTO, @NotNull Boolean isInsuranceEnabled, @NotNull Boolean isSalaryClient) {

        BigDecimal totalAmount = loanApplicationRequestDTO.getAmount();

        if (isInsuranceEnabled) {
            totalAmount = totalAmount.add(IS_INSURANCE_RATE.multiply(totalAmount).divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP));
        }
        log.info("Расчет актуального размера тела кредита");

        BigDecimal finalRate = changeRateByIsInsuranceEnabledOrIsSalaryClient(isInsuranceEnabled, isSalaryClient);
        log.info("Завершен расчет актуального размера кредитной ставки: " + finalRate+ " % ");

        log.info("Расчет сотой доли месячной ставки ");
        BigDecimal aHundredthPartOfMonthlyRate = finalRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP).setScale(8, RoundingMode.HALF_UP);
        log.info("Сотая доля месячной ставки составляет  " + aHundredthPartOfMonthlyRate.setScale(2,RoundingMode.HALF_UP));

        log.info("Расчет ежемесячного платежа ");
        BigDecimal monthlyPayment = totalAmount.multiply((aHundredthPartOfMonthlyRate
                .add((aHundredthPartOfMonthlyRate
                        .divide(((BigDecimal.valueOf(1)
                                .add(aHundredthPartOfMonthlyRate))
                                .pow(loanApplicationRequestDTO.getTerm()))
                                .subtract(BigDecimal.valueOf(1)), 8, RoundingMode.HALF_UP))))).setScale(2, RoundingMode.HALF_UP);

        log.info("Ежемесячный платеж с учетом ануитетного графика погашения составляет " + monthlyPayment);

        return new LoanOfferDTO()
                .applicationId((long) new Random().nextInt())
                .requestedAmount(loanApplicationRequestDTO.getAmount())
                .totalAmount(totalAmount)
                .term(loanApplicationRequestDTO.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(finalRate)
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient);
    }

    public BigDecimal changeRateByIsInsuranceEnabledOrIsSalaryClient(@NotNull Boolean isInsuranceEnabled, @NotNull Boolean isSalaryClient) {
        BigDecimal finalRate = BASE_RATE;

        if (isSalaryClient) {
            finalRate = finalRate.subtract(BigDecimal.valueOf(1));
        }
        if (isInsuranceEnabled) {
            finalRate = finalRate.subtract(BigDecimal.valueOf(3));
        }

        return finalRate;
    }

}






