package hyperface.cms.util.validation

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PositiveIntegerValidator implements ConstraintValidator<PositiveInteger, Integer> {

    @Override
    boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false
        }
        return value > 0
    }
}
