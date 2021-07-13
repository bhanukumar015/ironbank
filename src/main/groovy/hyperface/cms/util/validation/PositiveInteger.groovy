package hyperface.cms.util.validation

import javax.validation.Constraint
import javax.validation.Payload
import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Documented
@Constraint(validatedBy = PositiveIntegerValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface PositiveInteger {

    String message() default "INVALID value"

    Class<?>[] groups() default []

    Class<? extends Payload>[] payload() default []
}