package hyperface.cms.util.validation

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class PositiveValueValidator implements ConstraintValidator <PositiveValue, Double> {

    @Override
    boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return false
        }
        return value > 0
    }
}
