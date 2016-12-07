package org.cresst.sb.irp.automation.adapter.proctor.data.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.cresst.sb.irp.automation.adapter.proctor.data.Accommodations.AccType;
import org.cresst.sb.irp.automation.adapter.proctor.data.Accommodations.AccTypes;

import java.io.IOException;

public class AccTypesDeserializer extends StdDeserializer<AccTypes> {

    protected AccTypesDeserializer() {
        super(AccTypes.class);
    }

    @Override
    public AccTypes deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        AccTypes hashMap = new AccTypes();
        JsonNode rootNode = jsonParser.readValueAsTree();

        for (JsonNode objectNode : rootNode) {
            JsonNode keyNode = objectNode.get("Key");
            JsonNode valueNode = objectNode.get("Value");
            JsonDeserializer<Object> deserializer = deserializationContext.findRootValueDeserializer(deserializationContext.getTypeFactory().constructType(AccType.class));

            JsonParser valueJsonParser = valueNode.traverse();
            valueJsonParser.nextToken();

            String key = keyNode.asText();
            AccType value = (AccType) deserializer.deserialize(valueJsonParser, deserializationContext);

            hashMap.put(key, value);
        }

        return hashMap;
    }
}
