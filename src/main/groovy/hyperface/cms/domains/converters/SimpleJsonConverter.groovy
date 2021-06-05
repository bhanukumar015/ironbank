package hyperface.cms.domains.converters

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator
import hyperface.cms.domains.fees.Fee
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class SimpleJsonConverter implements AttributeConverter<Object, String> {

    Logger log = LoggerFactory.getLogger(SimpleJsonConverter.class);

    private static ObjectMapper mapper
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    String convertToDatabaseColumn(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            return null;
            // or throw an error
        }
    }

    @Override
    Object convertToEntityAttribute(String dbData) {
        log.info "Converting from DB:" + dbData
        if(dbData == "{}") { return null }
        return objectMapper.readValue(dbData, Object.class);
    }
}
