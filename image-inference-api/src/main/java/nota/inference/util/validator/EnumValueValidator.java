package nota.inference.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Locale;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
    private Enum<?>[] enumValues;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        enumValues = constraintAnnotation.enumClass().getEnumConstants();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null)
            return false;
        String valueToUpperCase = value.toUpperCase(Locale.ROOT);
        return Arrays.stream(enumValues)
                .anyMatch(enumValue -> enumValue.toString().equals(valueToUpperCase));
    }
}
