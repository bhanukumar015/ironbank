package hyperface.cms.util

import org.apache.commons.lang3.RandomStringUtils

class Utilities {

    private Utilities() {}

    static String generateUniqueReference(int length) {
        return RandomStringUtils.randomAlphanumeric(length).toLowerCase()
    }
}
