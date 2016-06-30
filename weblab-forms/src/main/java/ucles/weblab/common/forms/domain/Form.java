package ucles.weblab.common.forms.domain;

import java.time.Instant;
import java.util.List;
import org.immutables.value.Value;

/**
 *
 * @author Sukhraj
 */

@Value.Immutable
public interface Form {
        
    String getId();
    String getName();
    String getDescription();
    String getSchema();
    List<String> getBusinessStreams();
    String getApplicationName();
    Instant getValidFrom();
    Instant getValidTo();
}
