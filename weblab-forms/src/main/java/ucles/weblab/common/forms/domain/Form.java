package ucles.weblab.common.forms.domain;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.immutables.value.Value;

/**
 *
 * @author Sukhraj
 */

@Value.Immutable
public interface Form {
        
    String getName();
    JsonSchema getSchema();
    String getBusinessStream();
    String getApplicationName();
}
