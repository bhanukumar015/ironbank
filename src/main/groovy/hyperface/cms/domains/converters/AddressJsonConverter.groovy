package hyperface.cms.domains.converters

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.domains.Address
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class AddressJsonConverter implements AttributeConverter<Address, String> {

    Logger log = LoggerFactory.getLogger(AddressJsonConverter.class);

    private static ObjectMapper mapper
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    String convertToDatabaseColumn(Address object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            return null;
            // or throw an error
        }
    }

    @Override
    Address convertToEntityAttribute(String dbData) {
        log.info "Converting from DB:" + dbData
        if (dbData == "{}" || dbData == null) {
            return null
        }
        return objectMapper.readValue(dbData, Address.class);
    }
}
