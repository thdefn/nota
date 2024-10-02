package nota.inference.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeValidator implements ConstraintValidator<DateTime, String> {
    private DateTimeFormatter dateTimeFormatter;
    private boolean nullable;

    @Override
    public void initialize(DateTime constraintAnnotation) {
        dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null && nullable)
            return true;
        else if (value == null)
            return false;
        try {
            LocalDateTime.parse(value, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
