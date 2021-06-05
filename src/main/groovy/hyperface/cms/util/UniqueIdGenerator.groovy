package hyperface.cms.util

import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.fees.Fee
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator

class UniqueIdGenerator implements IdentifierGenerator {

    @Override
    Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        String prefix = "obj_"
        if(object instanceof Customer) {
            return "cst_" + generateRandomString(14)
        }
        else if(object instanceof Card) {
            return "card_" + generateRandomString(16)
        }
        else if(object instanceof CustomerTxn) {
            return "txn_" + generateRandomString(16)
        }
        else if(object instanceof  CreditCardProgram) {
            return "ccp_" + generateRandomString(16)
        }
        else if(object instanceof Fee) {
            return "fee_" + generateRandomString(16)
        }
        return UUID.randomUUID().toString()
    }

    private static String generateRandomString(int targetStringLength = 14) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

}
