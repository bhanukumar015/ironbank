package hyperface.cms.domains.converters


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator
import hyperface.cms.domains.fees.FeeStrategy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
class FeeStrategyJsonConverter implements AttributeConverter<FeeStrategy, String> {


    Logger log = LoggerFactory.getLogger(FeeStrategyJsonConverter.class);

    private static ObjectMapper mapper
    static {
        // To avoid instantiating ObjectMapper again and again.
//        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(FeeStrategy.class).build()
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().build()
        mapper = JsonMapper.builder().build()
//        mapper.enableDefaultTyping()
    }

    @Override
    String convertToDatabaseColumn(FeeStrategy strategy) {
        if (null == strategy) {
            // You may return null if you prefer that style
            return "{}";
        }
        String stringRep = mapper.writeValueAsString(strategy)
        log.info stringRep
        return stringRep
    }

    @Override
    FeeStrategy convertToEntityAttribute(String dbData) {
        log.info dbData
        if(dbData == "{}") { return null }
        return mapper.readValue(dbData, FeeStrategy.class)
    }
}
