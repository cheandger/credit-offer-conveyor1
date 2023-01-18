package com.test.offerservice.impl;


import com.shrek.exceptions.ParametersValidationException;
import com.shrek.model.LoanApplicationRequestDTO;
import com.shrek.model.LoanOfferDTO;
import com.shrek.servises.OffersService;
import com.shrek.servises.impl.OffersServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static com.test.offerservice.impl.loanapplreqdtoconfig.LoanApplicationRequestDTOInitializer.loanAppReqInit;


class OffersServiceImplTest {

    private final BigDecimal BASE_RATE = BigDecimal.valueOf(27.00).setScale(2, RoundingMode.HALF_UP);


    @BeforeAll
    static void setUp() {
        ReflectionTestUtils.setField(offersService, "BASE_RATE", BigDecimal.valueOf(27.00).setScale(2, RoundingMode.HALF_UP));
        ReflectionTestUtils.setField(offersService, "IS_INSURANCE_RATE", BigDecimal.valueOf(3.00).setScale(2, RoundingMode.HALF_UP));

    }

    static OffersService offersService = new OffersServiceImpl();

    @Test
    @DisplayName("Test valid age more than 18 years Old")
    void validAgeMoreThanEighteenTest() {
        LoanApplicationRequestDTO validAgeMoreThanEighteen = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("1985-01-29"));
        Assertions.assertDoesNotThrow(()-> offersService.offers(validAgeMoreThanEighteen), "Test passed");
    }
    @Test
    @DisplayName("Test nonValid age less than 18 years Old")
    void validAgeLessThanEighteen() {
        LoanApplicationRequestDTO validAgeLessThanEighteen = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("2007-01-29"));
        Assertions.assertThrows(ParametersValidationException.class,()-> offersService.offers(validAgeLessThanEighteen), "Test passed");
    }
    @Test
    @DisplayName("Test creating of full LoanOffer List with different rate values, depend on IsInsuranceEnabled Or IsSalaryClient fields ")
    void creatingOfFullListOfRateChangesByIsInsuranceEnabledOrIsSalaryClientTest() {
        LoanApplicationRequestDTO validLoanApplicationRequestDTO = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("1985-01-29"));

List<LoanOfferDTO> fullListOfRateChangesByIsInsuranceEnabledOrIsSalaryClient=offersService.offers(validLoanApplicationRequestDTO);
                Assertions.assertEquals(BASE_RATE.setScale(2,RoundingMode.HALF_UP), fullListOfRateChangesByIsInsuranceEnabledOrIsSalaryClient.get(0).getRate());
                Assertions.assertEquals(BASE_RATE.subtract(BigDecimal.valueOf(1)), fullListOfRateChangesByIsInsuranceEnabledOrIsSalaryClient.get(1).getRate());
                Assertions.assertEquals(BASE_RATE.subtract(BigDecimal.valueOf(3)), fullListOfRateChangesByIsInsuranceEnabledOrIsSalaryClient.get(2).getRate());
                Assertions.assertEquals(BASE_RATE.subtract(BigDecimal.valueOf(4)), fullListOfRateChangesByIsInsuranceEnabledOrIsSalaryClient.get(3).getRate());

    }

    @Test
    @DisplayName("Test rate check IsInsuranceEnabled true case")
    void rateCheckIsInsuranceEnabledTrueLoanOffer() {
        LoanApplicationRequestDTO IsInsuranceEnabledTrue = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("1985-01-29"));
        LoanOfferDTO IsInsuranceEnabledTrueLoanOfferDTO = offersService.createLoanOffer(IsInsuranceEnabledTrue,true,false);
Assertions.assertEquals(BASE_RATE.subtract(BigDecimal.valueOf(3)),IsInsuranceEnabledTrueLoanOfferDTO.getRate());
    }
    @Test
    @DisplayName("Test rate check IsInsuranceEnabled false case")
    void rateCheckIsInsuranceEnabledFalseLoanOffer() {
        LoanApplicationRequestDTO IsInsuranceEnabledFalse = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("1985-01-29"));
        LoanOfferDTO IsInsuranceEnabledFalseLoanOfferDTO = offersService.createLoanOffer(IsInsuranceEnabledFalse,false,false);
        Assertions.assertEquals(BASE_RATE,IsInsuranceEnabledFalseLoanOfferDTO.getRate());

    }
    @Test
    @DisplayName("Test rate check IsSalary false case")
    void rateCheckIsSalaryFalseLoanOffer() {
        LoanApplicationRequestDTO IsSalaryFalse = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("1985-01-29"));
        LoanOfferDTO IsSalaryFalseFalseLoanOfferDTO = offersService.createLoanOffer(IsSalaryFalse,false,false);
        Assertions.assertEquals(BASE_RATE,IsSalaryFalseFalseLoanOfferDTO.getRate());

    }
    @Test
    @DisplayName("Test rate check IsSalary True case")
    void rateCheckIsSalaryTrueLoanOffer() {
        LoanApplicationRequestDTO IsSalaryTrue = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("1985-01-29"));
        LoanOfferDTO IsSalaryTrueFalseLoanOfferDTO = offersService.createLoanOffer(IsSalaryTrue,false,true);
        Assertions.assertEquals(BASE_RATE.subtract(BigDecimal.valueOf(1)),IsSalaryTrueFalseLoanOfferDTO.getRate());

    }
    @Test
    @DisplayName("Test monthlyPayment check Is not NULL  case")
    void rateCheckMonthlyPaymentIsNotNullLoanOffer() {
        LoanApplicationRequestDTO monthlyPaymentNotnull = loanAppReqInit(BigDecimal.valueOf(100000), 12, LocalDate.parse("1985-01-29"));
        LoanOfferDTO monthlyPaymentNotnullLoanOfferDTO = offersService.createLoanOffer(monthlyPaymentNotnull,false,true);

        Assertions.assertNotNull(monthlyPaymentNotnullLoanOfferDTO.getMonthlyPayment());

    }



}