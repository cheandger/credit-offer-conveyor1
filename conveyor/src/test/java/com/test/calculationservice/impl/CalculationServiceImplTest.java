package com.test.calculationservice.impl;

import com.shrek.exceptions.ParametersValidationException;
import com.shrek.model.EmploymentDTO;
import com.shrek.model.ScoringDataDTO;
import com.shrek.servises.CalculationService;
import com.shrek.servises.impl.CalculationServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static com.shrek.model.EmploymentDTO.EmploymentStatusEnum.*;
import static com.shrek.model.EmploymentDTO.PositionEnum.*;
import static com.shrek.model.ScoringDataDTO.GenderEnum.*;
import static com.shrek.model.ScoringDataDTO.MaritalStatusEnum.*;
import static com.test.calculationservice.impl.scoringdatadtoconfig.ScoringDataDtoInitializer.initialEmploymentDTO;
import static com.test.calculationservice.impl.scoringdatadtoconfig.ScoringDataDtoInitializer.initialScoringDataDTO;


public class CalculationServiceImplTest {

    private final BigDecimal BASE_RATE = BigDecimal.valueOf(27.00).setScale(2, RoundingMode.HALF_UP);

    private final BigDecimal IS_INSURANCE_RATE = BigDecimal.valueOf(3.00).setScale(2, RoundingMode.HALF_UP);

@BeforeAll
static void setUp() {
        ReflectionTestUtils.setField(calculationService, "BASE_RATE", BigDecimal.valueOf(27.00).setScale(2, RoundingMode.HALF_UP));
        ReflectionTestUtils.setField(calculationService, "IS_INSURANCE_RATE", BigDecimal.valueOf(3.00).setScale(2, RoundingMode.HALF_UP));

    }

    static CalculationService calculationService = new CalculationServiceImpl();

    @Test
    @DisplayName("Test calculation by Valid EmploymentStatus SELF_EMPLOYED")
    void validEmploymentStatusSelfEmployedTest() {

        EmploymentDTO validEmploymentDTOStatusSELF_EMPLOYED = initialEmploymentDTO(SELF_EMPLOYED, BigDecimal.valueOf(60000), WORKER, 56, 12);
        ScoringDataDTO validEmploymentScoringDataDTOEmploymentStatusSELF_EMPLOYED = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE, LocalDate.parse("1985-01-29"), MARRIED, 1,
                validEmploymentDTOStatusSELF_EMPLOYED, true, true);

        Assertions.assertDoesNotThrow(() ->
                calculationService.calculation(validEmploymentScoringDataDTOEmploymentStatusSELF_EMPLOYED), "Test passed!");
    }

    @Test
    @DisplayName("Test calculation by Valid EmploymentStatus BUSINESS_OWNER")
    void validEmploymentStatusBusinessOwnerTest() {

        EmploymentDTO validEmploymentDTOStatusBUSSINESS_OWNER = initialEmploymentDTO(BUSINESS_OWNER, BigDecimal.valueOf(180000), WORKER, 80, 48);
        ScoringDataDTO validEmploymentScoringDataDTOEmploymentStatusBUSSINESS_OWNER = initialScoringDataDTO(BigDecimal.valueOf(500000), 24, MALE, LocalDate.parse("1985-01-29"), MARRIED, 2,
                validEmploymentDTOStatusBUSSINESS_OWNER, false, true);

        Assertions.assertDoesNotThrow(() ->
        {

            calculationService.calculation(validEmploymentScoringDataDTOEmploymentStatusBUSSINESS_OWNER);
        }, "Test passed!");
    }

    @Test
    @DisplayName("Test calculation by NonValid EmploymentStatus Unemployed")
    void nonValidUnemployedEmploymentStatusTest() {
        EmploymentDTO UNEMPLOYEDEmploymentStatusDTO = initialEmploymentDTO(UNEMPLOYED, BigDecimal.valueOf(60000), WORKER, 56, 12);
        ScoringDataDTO UNEMPLOYEDScoringDataDTO = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE, LocalDate.parse("1985-01-29"), MARRIED, 1,
                UNEMPLOYEDEmploymentStatusDTO, true, true);

        Assertions.assertThrows(ParametersValidationException.class, () ->
                calculationService.calculation(UNEMPLOYEDScoringDataDTO), "We can't help you. Please find a jod and try again");
    }

    @Test
    @DisplayName("Test calculation by NonValid Little Salary Large loan Amount. The loan amount is 20 times the salary")
    void nonValidLittleSalaryLargeAmountTest() {
        EmploymentDTO NonValid_LittleSalary_LargeAmount_EmploymentDTO = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(30000), WORKER, 56, 12);
        ScoringDataDTO NonValid_LittleSalary_LargeAmount_ScoringDataDTO = initialScoringDataDTO(BigDecimal.valueOf(1000000), 12, MALE, LocalDate.parse("1985-01-29"), MARRIED, 1,
                NonValid_LittleSalary_LargeAmount_EmploymentDTO, true, true);
//
        Assertions.assertThrows(ParametersValidationException.class, () ->
                calculationService.calculation(NonValid_LittleSalary_LargeAmount_ScoringDataDTO), "The sum you are want to loan is too large");
    }

    @Test
    @DisplayName("Test calculation by NonValid birthDate too young. The Age is less than 20 yearsOld")
    void nonValidAgeTooYoungTest() {
        EmploymentDTO NonValid_Age_TooYoung_EmploymentDTO = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(80000), WORKER, 56, 12);
        ScoringDataDTO NonValid_Age_TooYoung_ScoringDataDTO = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE, LocalDate.parse("2005-01-29"), MARRIED, 1,
                NonValid_Age_TooYoung_EmploymentDTO, true, true);
//The Age is less than 20 yearsOld
        Assertions.assertThrows(ParametersValidationException.class, () ->
                calculationService.calculation(NonValid_Age_TooYoung_ScoringDataDTO), "The Age value is invalid. It should be >20||<60");
    }

    @Test
    @DisplayName("Test calculation by NonValid birthDate too Old. The Age is more than 60 yearsOld")
    void nonValidAgeTooOldTest() {
        EmploymentDTO NonValid_Age_TooOld_EmploymentDTO = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(80000), WORKER, 56, 12);
        ScoringDataDTO NonValid_Age_TooOld_ScoringDataDTO = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE, LocalDate.parse("1962-01-29"), MARRIED, 1,
                NonValid_Age_TooOld_EmploymentDTO, true, true);

        Assertions.assertThrows(ParametersValidationException.class, () ->
                calculationService.calculation(NonValid_Age_TooOld_ScoringDataDTO), "The Age value is invalid. It should be >20||<60");
    }

    @Test
    @DisplayName("Test calculation by NonValid WorkExperience Total. The workExperienceTotal is less than 12 months")
    void nonValidWorkExperienceTotalTest() {

        EmploymentDTO NonValid_workExperienceTotal_EmploymentDTO = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(80000), WORKER, 9, 12);
        ScoringDataDTO NonValid_workExperienceTotal_ScoringDataDTO = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE, LocalDate.parse("1985-01-29"), MARRIED, 1,
                NonValid_workExperienceTotal_EmploymentDTO, true, true);

        Assertions.assertThrows(ParametersValidationException.class, () ->
                calculationService.calculation(NonValid_workExperienceTotal_ScoringDataDTO), "Too little workExperienceTotal. It should be more than 12 month.");
    }

    @Test
    @DisplayName("Test calculation by NonValid WorkExperience Current. The workExperienceCurrent is less than 3 months")
    void nonValidWorkExperienceCurrentTest() {

        EmploymentDTO NonValid_workExperienceCurrent_EmploymentDTO = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(80000), WORKER, 12, 2);
        ScoringDataDTO NonValid_workExperienceCurrent_ScoringDataDTO = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE, LocalDate.parse("1985-01-29"), MARRIED, 1,
                NonValid_workExperienceCurrent_EmploymentDTO, true, true);

        Assertions.assertThrows(ParametersValidationException.class, () ->
                calculationService.calculation(NonValid_workExperienceCurrent_ScoringDataDTO), "Too little workExperienceCurrent. It should be more than 3 month.");
    }


    @Test
    @DisplayName("Test calculation by isInsuranceEnabled true. It'll decrease BaseRate by 3 but increase the fullLoanAmount by (amount*IS_INSURANCE_RATE percents/100) value")
    void isInsuranceEnabledTrueReducedRateByThreeScoringDTOTest() {
        EmploymentDTO employmentDTOis_INSURANCE_RATETrue = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTOis_INSURANCE_RATETrue = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTOis_INSURANCE_RATETrue, true, false);

        BigDecimal fullLoanAmount = calculationService//Finding FullLoanAmount sum to compare it with expected result
                .calculation(scoringDataDTOis_INSURANCE_RATETrue).getPaymentSchedule().get(0).getRemainingDebt().
                add(calculationService
                        .calculation(scoringDataDTOis_INSURANCE_RATETrue).getPaymentSchedule().get(0).getDebtPayment());

        Assertions.assertAll("Check the fullLoanAmount value calculation and the result RATE", () -> {
                    Assertions.assertEquals(fullLoanAmount,

                            scoringDataDTOis_INSURANCE_RATETrue.getAmount().add((IS_INSURANCE_RATE.multiply(scoringDataDTOis_INSURANCE_RATETrue.getAmount())
                                    .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP))
                                    .divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(scoringDataDTOis_INSURANCE_RATETrue.getTerm()))).setScale(2, RoundingMode.HALF_UP));
                }
                , () -> {
                    Assertions.assertEquals(calculationService.calculation(scoringDataDTOis_INSURANCE_RATETrue).getRate(), BASE_RATE.subtract(BigDecimal.valueOf(3.00)));

                });
    }


    @Test
    @DisplayName("Test calculation by isInsuranceEnabled false. The rate and the amount will remain unchanged")
    void isInsuranceEnabledFalseSameRateScoringDTOTest() {
        EmploymentDTO employmentDTOis_INSURANCE_RATEFalse = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTOis_INSURANCE_RATEFalse = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTOis_INSURANCE_RATEFalse, false, false);

        BigDecimal fullLoanAmount = calculationService//Finding FullLoanAmount sum to compare it with expected result
                .calculation(scoringDataDTOis_INSURANCE_RATEFalse).getPaymentSchedule().get(0).getRemainingDebt().
                add(calculationService
                        .calculation(scoringDataDTOis_INSURANCE_RATEFalse).getPaymentSchedule().get(0).getDebtPayment());

        Assertions.assertAll("Check the fullLoanAmount value calculation and the result RATE", () -> {
                    Assertions.assertEquals(fullLoanAmount, scoringDataDTOis_INSURANCE_RATEFalse.getAmount().setScale(2, RoundingMode.HALF_UP));
                }
                , () -> {
                    Assertions.assertEquals(calculationService.calculation(scoringDataDTOis_INSURANCE_RATEFalse).getRate(), BASE_RATE);
                    //Checking the final rate to compare it with expected rate
                });
    }

    @Test
    @DisplayName("Test calculation by isSalary true case. The rate will decrease by 1 and the amount will remain unchanged")
    void isSalaryTrueRateReducedByOneScoringDTOTest() {
        EmploymentDTO employmentDTOis_Salary_True = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTOis_Salary_True = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTOis_Salary_True, false, true);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTOis_Salary_True).getRate(), BASE_RATE.subtract(BigDecimal.valueOf(1)));
    }

    @Test
    @DisplayName("Test calculation by isSalary false case. The rate will remain unchanged")
    void isSalaryFalseBaseRateScoringDTOTest() {
        EmploymentDTO employmentDTOis_Salary_False = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTOis_Salary_False = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTOis_Salary_False, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTOis_Salary_False).getRate(), BASE_RATE);
    }

    @Test
    @DisplayName("Test calculation by employmentStatus SELF_EMPLOYED case. The rate will increase by 1")
    void employmentStatusSelfEmployedRateIncreasedByOneScoringDTOTest() {
        EmploymentDTO employmentDTO_EmploymentStatus_SELF_EMPLOYED_Rate_increased_By_One = initialEmploymentDTO(SELF_EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTOis_EmploymentStatus_SELF_EMPLOYED_Rate_increased_By_One = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTO_EmploymentStatus_SELF_EMPLOYED_Rate_increased_By_One, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTOis_EmploymentStatus_SELF_EMPLOYED_Rate_increased_By_One).getRate(), BASE_RATE.add(BigDecimal.valueOf(1)));
    }

    @Test
    @DisplayName("Test calculation by employmentStatus BUSINESS_OWNER case. The rate will increase by 3")
    void employmentStatusBusinessOwnerRateIncreasedByThreeScoringDTOTest() {
        EmploymentDTO employmentDTO_EmploymentStatus_BUSSINESS_OWNER_Rate_increased_By_Three = initialEmploymentDTO(BUSINESS_OWNER, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_EmploymentStatus_BUSSINESS_OWNER_Rate_increased_By_Three = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTO_EmploymentStatus_BUSSINESS_OWNER_Rate_increased_By_Three, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_EmploymentStatus_BUSSINESS_OWNER_Rate_increased_By_Three).getRate(), BASE_RATE.add(BigDecimal.valueOf(3)));

    }

    @Test
    @DisplayName("Test calculation by employmentStatus MID_MANAGER case. The rate will decrease by 2")
    void employmentGetPositionMidManagerRateSubtractByTwoScoringDTOTest() {
        EmploymentDTO employmentDTO_GetPosition_MID_MANAGER_Rate_subtract_By_Two = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                MID_MANAGER, 56, 12);

        ScoringDataDTO scoringDataDTO_GetPosition_MID_MANAGER_Rate_subtract_By_Two = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTO_GetPosition_MID_MANAGER_Rate_subtract_By_Two, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_GetPosition_MID_MANAGER_Rate_subtract_By_Two).getRate(), BASE_RATE.subtract(BigDecimal.valueOf(2)));
    }

    @Test
    @DisplayName("Test calculation by employmentStatus TOP_MANAGER case. The rate will decrease by 4")
    void employmentTopManagerRateSubtractByFourScoringDTOTest() {
        EmploymentDTO employmentDTO_GetPosition_TOP_MANAGER_Rate_subtract_By_Four = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                TOP_MANAGER, 56, 12);

        ScoringDataDTO scoringDataDTO_GetPosition_TOP_MANAGER_Rate_subtract_By_Four = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 0,
                employmentDTO_GetPosition_TOP_MANAGER_Rate_subtract_By_Four, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_GetPosition_TOP_MANAGER_Rate_subtract_By_Four).getRate(), BASE_RATE.subtract(BigDecimal.valueOf(4)));

    }

    @Test
    @DisplayName("Test calculation by maritalStatus MARRIED case. The rate will decrease by 3")
    void maritalStatusMarriedRateSubtractByThreeScoringDTOTest() {
        EmploymentDTO employmentDTO_MaritalStatus_MARRIED_Rate_subtract_By_Three = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_MaritalStatus_MARRIED_Rate_subtract_By_Three = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), MARRIED, 0,
                employmentDTO_MaritalStatus_MARRIED_Rate_subtract_By_Three, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_MaritalStatus_MARRIED_Rate_subtract_By_Three).getRate(), BASE_RATE.subtract(BigDecimal.valueOf(3)));

    }

    @Test
    @DisplayName("Test calculation by maritalStatus DIVORCED case. The rate will increase by 1")
        void maritalStatusDivorcedRateIncreasedByOneScoringDTOTest() {

        EmploymentDTO employmentDTO_MaritalStatus_DIVORCED_Rate_increased_By_One = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_MaritalStatus_DIVORCED_Rate_increased_By_One = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), DIVORCED, 0,
                employmentDTO_MaritalStatus_DIVORCED_Rate_increased_By_One, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_MaritalStatus_DIVORCED_Rate_increased_By_One).getRate(), BASE_RATE.add(BigDecimal.valueOf(1)));
    }

    @Test
    @DisplayName("Test calculation by maritalStatus WIDOW_WIDOWER case. The rate will remain unchanged")
    void maritalStatusWidowWidowerSameRateScoringDTOTest() {

        EmploymentDTO employmentDTO_MaritalStatus_WIDOW_WIDOWER_Same_Rate = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_MaritalStatus_WIDOW_WIDOWER_Same_Rate = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), WIDOW_WIDOWER, 0,
                employmentDTO_MaritalStatus_WIDOW_WIDOWER_Same_Rate, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_MaritalStatus_WIDOW_WIDOWER_Same_Rate).getRate(), BASE_RATE);
    }

    @Test
    @DisplayName("Test calculation by DependentAmount more than 1 case. The rate will increase by 1")
    void getDependentAmountMoreThanOneRateIncreaseByOneScoringDTOTest() {

        EmploymentDTO employmentDTO_getDependentAmount_More_Than_One_Rate_increased_By_One = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_getDependentAmount_More_Than_One_Rate_increased_By_One = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1999-01-29"), SINGLE, 2,
                employmentDTO_getDependentAmount_More_Than_One_Rate_increased_By_One, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_getDependentAmount_More_Than_One_Rate_increased_By_One).getRate(), BASE_RATE.add(BigDecimal.valueOf(1)));
    }

    @Test
    @DisplayName("Test calculation by FEMALE age less than Sixty & more than ThirtyFive case. The rate will decrease by 3")
    void genderFemaleAgeLessSixtyMoreThirtyFiveSubtractRatebyThreeScoringDTOTest() {

        EmploymentDTO employmentDTO_gender_FEMALE_Age_Less_Sixty_More_ThirtyFive_subtract_Rate_by_Three = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_gender_FEMALE_Age_Less_Sixty_More_ThirtyFive_subtract_Rate_by_Three = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, FEMALE,
                LocalDate.parse("1978-01-29"), SINGLE, 0,
                employmentDTO_gender_FEMALE_Age_Less_Sixty_More_ThirtyFive_subtract_Rate_by_Three, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_gender_FEMALE_Age_Less_Sixty_More_ThirtyFive_subtract_Rate_by_Three).getRate(), BASE_RATE.subtract(BigDecimal.valueOf(3)));
    }

    @Test
    @DisplayName("Test calculation by FEMALE age less than ThirtyFive case. The rate will remain unchanged")
    void gender_FEMALE_Age_Less_ThirtyFive_BASE_Rate_ScoringDTOTest() {

        EmploymentDTO employmentDTO_gender_FEMALE_Age_Less_ThirtyFive_BASE_Rate = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_gender_FEMALE_Age_Less_ThirtyFive_BASE_Rate = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, FEMALE,
                LocalDate.parse("1995-01-29"), SINGLE, 0,
                employmentDTO_gender_FEMALE_Age_Less_ThirtyFive_BASE_Rate, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_gender_FEMALE_Age_Less_ThirtyFive_BASE_Rate).getRate(), BASE_RATE);
    }

    @Test
    @DisplayName("Test calculation by MALE age less than Thirty case. The rate will remain unchanged")
    void genderMaleAgeLessThirtyBASERateScoringDTOTest() {

        EmploymentDTO employmentDTO_gender_MALE_Age_Less_Thirty_BASE_Rate = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_gender_MALE_Age_Less_Thirty_BASE_Rate = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("2000-01-29"), SINGLE, 0,
                employmentDTO_gender_MALE_Age_Less_Thirty_BASE_Rate, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_gender_MALE_Age_Less_Thirty_BASE_Rate).getRate(), BASE_RATE);
    }

    @Test
    @DisplayName("Test calculation by MALE age less than FiftyFive more than Thirty case. The rate will decrease by 3")
    void genderMailAgeLessFiftyFiveMoreThirtySubtractRateByThreeScoringDTOTest() {

        EmploymentDTO employmentDTO_gender_MALE_Age_Less_FiftyFive_More_Thirty_subtract_Rate_by_Three = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_gender_MALE_Age_Less_FiftyFive_More_Thirty_subtract_Rate_by_Three = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1985-01-29"), SINGLE, 0,
                employmentDTO_gender_MALE_Age_Less_FiftyFive_More_Thirty_subtract_Rate_by_Three, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_gender_MALE_Age_Less_FiftyFive_More_Thirty_subtract_Rate_by_Three).getRate(), BASE_RATE.subtract(BigDecimal.valueOf(3)));
    }

    @Test
    @DisplayName("Test calculation by MALE age  more than FiftyFive case. The rate will remain unchanged")
    void genderMaleAgeMoreFiftyFiveBASERateScoringDTOTest() {

        EmploymentDTO employmentDTO_gender_MALE_Age_More_FiftyFive_BASE_Rate = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_gender_MALE_Age_More_FiftyFive_BASE_Rate = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, MALE,
                LocalDate.parse("1963-01-29"), SINGLE, 0,
                employmentDTO_gender_MALE_Age_More_FiftyFive_BASE_Rate, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_gender_MALE_Age_More_FiftyFive_BASE_Rate).getRate(), BASE_RATE);
    }

    @Test
    @DisplayName("Test calculation by gender NON BINARY case. The rate will increase by 3")
    void genderNonBinaryIncreaseRateByThreeScoringDTOTest() {

        EmploymentDTO employmentDTO_gender_NON_BINARY_Increase_Rate_By_Three = initialEmploymentDTO(EMPLOYED, BigDecimal.valueOf(60000),
                WORKER, 56, 12);

        ScoringDataDTO scoringDataDTO_gender_NON_BINARY_Increase_Rate_By_Three = initialScoringDataDTO(BigDecimal.valueOf(100000), 12, NON_BINARY,
                LocalDate.parse("1963-01-29"), SINGLE, 0,
                employmentDTO_gender_NON_BINARY_Increase_Rate_By_Three, false, false);

        Assertions.assertEquals(calculationService.calculation(scoringDataDTO_gender_NON_BINARY_Increase_Rate_By_Three).getRate(), BASE_RATE.add(BigDecimal.valueOf(3)));
    }


}