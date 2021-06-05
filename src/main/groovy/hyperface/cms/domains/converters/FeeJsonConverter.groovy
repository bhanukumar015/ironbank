package hyperface.cms.domains.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator
import hyperface.cms.domains.fees.Fee
import hyperface.cms.domains.fees.FeeStrategy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class FeeJsonConverter implements AttributeConverter<Fee, String> {


    Logger log = LoggerFactory.getLogger(FeeJsonConverter.class);

    private static ObjectMapper mapper
    static {
        // To avoid instantiating ObjectMapper again and again.
//        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(FeeStrategy.class).build()
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().build()
        mapper = JsonMapper.builder().build()
//        mapper.enableDefaultTyping()
    }

    @Override
    String convertToDatabaseColumn(Fee fee) {
        if (null == fee) {
            // You may return null if you prefer that style
            return "{}";
        }
        String stringRep = mapper.writeValueAsString(fee)
        log.info "Serializing to string: " + stringRep
        return stringRep
    }

    @Override
    Fee convertToEntityAttribute(String dbData) {
        log.info "Converting from DB:" + dbData
        if(dbData == "{}") { return null }
        return mapper.readValue(dbData, Fee.class)
    }
}
