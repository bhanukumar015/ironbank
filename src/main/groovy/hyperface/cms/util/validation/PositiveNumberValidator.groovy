package hyperface.cms.util.validation

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PositiveNumberValidator implements ConstraintValidator<PositiveValue, Object> {

    @Override
    boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || !(value instanceof Number)) {
            return false
        }
        if (value instanceof Integer)
            return (Integer) value > 0
        else if (value instanceof Double)
            return (Double) value > 0.0D
        else if (value instanceof Long)
            return (Long) value > 0L
        else if (value instanceof Float)
            return (Float) value > 0.0F
    }
}
