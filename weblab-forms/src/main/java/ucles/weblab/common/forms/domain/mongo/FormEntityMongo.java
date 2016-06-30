package ucles.weblab.common.forms.domain.mongo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormEntity;

/**
 *
 * @author Sukhraj
 */
@Document(collection = "forms")
public class FormEntityMongo implements FormEntity {

    @Id
    private String id;
    
    private String name;
    private String schema;
    private String applicationName;
    private List<String> businessStreams;
    private String description;
    private Instant validFrom;
    private Instant validTo;
    
    @SuppressWarnings("UnusedDeclaration") // For Jackson
    protected FormEntityMongo() {
    }
    
    public FormEntityMongo(Form vo) {
        id = vo.getId();
        name = vo.getName();
        schema = vo.getSchema();
        applicationName = vo.getApplicationName();
        businessStreams = vo.getBusinessStreams();
        description = vo.getDescription();
        validFrom = vo.getValidFrom();
        validTo = vo.getValidTo();
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getBusinessStreams() {
        return businessStreams;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Instant getValidFrom() {
        return validFrom;
    }

    @Override
    public Instant getValidTo() {
        return validTo;
    }
    
}
