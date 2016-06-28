package ucles.weblab.common.forms.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import java.time.Instant;
import org.immutables.value.Value;

/**
 *
 * @author Sukhraj
 */

@Value.Immutable
public interface Form {
        
    String getName();
    String getDescription();
    String getSchema();
    String getBusinessStream();
    String getApplicationName();
    Instant getValidFrom();
    Instant getValidTo();
}
