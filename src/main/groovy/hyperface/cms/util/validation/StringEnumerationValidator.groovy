package hyperface.cms.util.validation

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class StringEnumerationValidator implements ConstraintValidator<StringEnumeration, String> {

    private Set<String> AVAILABLE_ENUM_NAMES

    static Set<String> getNamesSet(Class<? extends Enum<?>> e) {
        Enum<?>[] enums = e.getEnumConstants()
        String[] names = new String[enums.length]
        for (int i = 0; i < enums.length; i++) {
            names[i] = enums[i].name()
        }
        Set<String> mySet = new HashSet<String>(Arrays.asList(names))
        return mySet
    }

    @Override
    void initialize(StringEnumeration stringEnumeration) {
        Class<? extends Enum<?>> enumSelected = stringEnumeration.enumClass()
        AVAILABLE_ENUM_NAMES = getNamesSet(enumSelected)
    }

    @Override
    boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false
        }
        return AVAILABLE_ENUM_NAMES.contains(value)
    }
}
