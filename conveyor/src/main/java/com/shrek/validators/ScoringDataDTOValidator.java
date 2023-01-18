package com.shrek.validators;



import com.shrek.model.EmploymentDTO;
import com.shrek.model.ScoringDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import static com.shrek.model.EmploymentDTO.EmploymentStatusEnum.UNEMPLOYED;

/*Рабочий статус: Безработный → отказ; Самозанятый → ставка увеличивается на 1; Владелец бизнеса → ставка увеличивается на 3
Позиция на работе: Менеджер среднего звена → ставка уменьшается на 2; Топ-менеджер → ставка уменьшается на 4
Сумма займа больше, чем 20 зарплат → отказ
Семейное положение: Замужем/женат → ставка уменьшается на 3; Разведен → ставка увеличивается на 1
Количество иждивенцев больше 1 → ставка увеличивается на 1
Возраст менее 20 или более 60 лет → отказ
Пол: Женщина, возраст от 35 до 60 лет → ставка уменьшается на 3; Мужчина, возраст от 30 до 55 лет → ставка уменьшается на 3; Не бинарный → ставка увеличивается на 3
Стаж работы: Общий стаж менее 12 месяцев → отказ; Текущий стаж менее 3 месяцев → отказ
*/
public class ScoringDataDTOValidator implements Validator {

    @Override

    public boolean supports(@NotNull Class<?> clazz) {
        return ScoringDataDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {

        ScoringDataDTO params = (ScoringDataDTO) target;
        EmploymentDTO employmentDTO = (EmploymentDTO) params.getEmployment();

        BigDecimal amount = params.getAmount();
        LocalDate birthdate = params.getBirthdate();
        String employmentStatus = String.valueOf(employmentDTO.getEmploymentStatus());
        BigDecimal salary = employmentDTO.getSalary();
        Integer workExperienceTotal = employmentDTO.getWorkExperienceTotal();
        Integer workExperienceCurrent = employmentDTO.getWorkExperienceCurrent();

        if (employmentStatus.equals(String.valueOf(UNEMPLOYED))) {
            errors.reject("employmentStatus", "We can't help you. Please find a jod and try again");
        }
        if (amount.compareTo(salary.multiply(BigDecimal.valueOf(20))) > 0) {
            errors.reject("amount,salary", "The sum you are want to loan is too large");
        }
        if ((LocalDate.now().getYear() - birthdate.getYear()) > 60 || (LocalDate.now().getYear() - birthdate.getYear()) < 20) {
            errors.reject("age", "Wrong age. It should be >20&<60");
        }
        if (workExperienceTotal < 12) {
            errors.reject("workExperienceTotal", "Too little workExperienceTotal");
        }
        if (workExperienceCurrent < 3) {
            errors.reject("workExperienceCurrent", "Too little workExperienceCurrent");
        }
    }
}
