package hyperface.cms.util.validation

import javax.validation.Constraint
import javax.validation.Payload
import java.lang.annotation.*

@Documented
@Constraint(validatedBy = PositiveValueValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface PositiveValue {

    String message() default "INVALID value"

    Class<?>[] groups() default []

    Class<? extends Payload>[] payload() default []
}