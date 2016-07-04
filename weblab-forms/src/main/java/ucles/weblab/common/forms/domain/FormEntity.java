package ucles.weblab.common.forms.domain;

import java.time.Instant;
import java.util.List;

/**
 *
 * @author Sukhraj
 */
public interface FormEntity extends Form {
    @Override
    List<String> getBusinessStreams();
    
    @Override
    String getApplicationName();
    
    boolean isNew();
    
    void setName(String name);
    void setDescription(String description);
    void setSchema(String schema);
    void setBusinessStreams(List<String> businessStreams);
    void setApplicationName(String applicationName);
    void setValidFrom(Instant validFrom);
    void setValidTo(Instant validTo);
}
