package hyperface.cms.domains.converters

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.domains.cardapplication.CardApplicationFlowStatus

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
@Slf4j
class CardApplicationFlowStatusConverter implements AttributeConverter<CardApplicationFlowStatus, String> {

    private static ObjectMapper mapper
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    String convertToDatabaseColumn(CardApplicationFlowStatus object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            return null;
            // or throw an error
        }
    }

    @Override
    CardApplicationFlowStatus convertToEntityAttribute(String dbData) {
        log.info "Converting from DB:" + dbData
        if (dbData == "{}" || dbData == null) {
            return null
        }
        return objectMapper.readValue(dbData, CardApplicationFlowStatus.class);
    }
}
