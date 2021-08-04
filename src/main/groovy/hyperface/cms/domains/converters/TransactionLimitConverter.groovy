package hyperface.cms.domains.converters

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.domains.Address
import hyperface.cms.domains.TransactionLimit

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
@Slf4j
class TransactionLimitConverter implements AttributeConverter<TransactionLimit, String> {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    String convertToDatabaseColumn(TransactionLimit object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            return null
        }
    }

    @Override
    TransactionLimit convertToEntityAttribute(String dbData) {
        log.info "Converting from DB:" + dbData
        if (dbData == "{}" || dbData == null) {
            return null
        }
        return objectMapper.readValue(dbData, TransactionLimit.class);
    }
}
