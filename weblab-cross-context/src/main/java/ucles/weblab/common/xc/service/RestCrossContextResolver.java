package ucles.weblab.common.xc.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Sukhraj
 */
public class RestCrossContextResolver implements CrossContextResolver {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RestCrossContextConverter converter;
    private final RestTemplate restTemplate;
    
    public RestCrossContextResolver(RestCrossContextConverter converter, RestTemplate restTemplate) {
        this.converter = converter;
        this.restTemplate = restTemplate;
    }
    
    @Override
    public JsonNode urnToJson(URI urn) {
        URI urlToCall = converter.toUrl(urn);
        if (urlToCall != null) {
            JsonNode node = objectMapper.convertValue(urlToCall, JsonNode.class);
            return node;
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
