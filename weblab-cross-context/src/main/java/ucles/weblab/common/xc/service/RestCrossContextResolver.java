package ucles.weblab.common.xc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

/**
 *
 * @author Sukhraj
 */
public class RestCrossContextResolver implements CrossContextResolver {

    private final RestCrossContextConverter converter;
    private final ObjectMapper objectMapper;
    
    public RestCrossContextResolver(RestCrossContextConverter converter,
                                    ObjectMapper objectMapper) {
        
        this.converter = converter;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public JsonNode urnToJson(URI urn) {
        URI urlToCall = converter.toUrl(urn);
        if (urlToCall != null) {
            return objectMapper.convertValue(urlToCall, JsonNode.class);
        }
        return null;
    }

    @Override
    public <T> T urnToValue(URI urn, Class<T> type) {
        URI urlToCall = converter.toUrl(urn);
        if (urlToCall != null) {
            return objectMapper.convertValue(urlToCall, type);
        }
        return null;
    }
    
}
