package hyperface.cms.util.validation

import javax.validation.Constraint
import javax.validation.Payload
import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Documented
@Constraint(validatedBy = StringEnumerationValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface StringEnumeration {

    String message() default "INVALID enum value"

    Class<?>[] groups() default []

    Class<? extends Payload>[] payload() default []

    Class<? extends Enum<?>> enumClass()

}