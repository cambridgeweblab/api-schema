package ucles.weblab.common.forms.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import java.util.UUID;

/**
 *
 * @author Sukhraj
 */
public interface Form {
        
    String getName();
    JsonSchema getSchema();
    String getBusinessStream();
    String getApplicationName();
}
