package com.test.calculationservice.impl.scoringdatadtoconfig;

import com.shrek.model.EmploymentDTO;
import com.shrek.model.ScoringDataDTO;




import java.math.BigDecimal;
import java.time.LocalDate;

public class ScoringDataDtoInitializer {




    public static ScoringDataDTO initialScoringDataDTO(BigDecimal amount, Integer term,
                                                       ScoringDataDTO.GenderEnum gender, LocalDate birthdate, ScoringDataDTO.MaritalStatusEnum maritalStatus,
                                                       Integer dependentAmount, EmploymentDTO employmentDTO,
                                                       Boolean isInsuranceEnabled, Boolean isSalaryClient) {


        return new ScoringDataDTO()
                .amount(amount)
                .term(term)
                .firstName("Вася")
                .lastName("Пупкин")
                .gender(gender)
                .birthdate(birthdate)
                .passportSeries("2355")
                .passportNumber("564836")
                .passportIssueDate(LocalDate.of(2005, 5, 5))
                .passportIssueBranch("Воронеж")
                .maritalStatus(maritalStatus)
                .dependentAmount(dependentAmount)
                .employment(employmentDTO)
                .account("johnyDepp")
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient);
    }
    public static EmploymentDTO initialEmploymentDTO (EmploymentDTO.EmploymentStatusEnum employmentStatus, BigDecimal salary, EmploymentDTO.PositionEnum position,
                                               Integer workExperienceTotal, Integer workExperienceCurrent){
        return new EmploymentDTO()
                .employmentStatus(employmentStatus)
                .employerINN("1112222333")
                .salary(salary)
                .position(position)
                .workExperienceTotal(workExperienceTotal)
                .workExperienceCurrent(workExperienceCurrent);

    }


}
