package ucles.weblab.common.forms.domain.mongo;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Document;
import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormEntity;

/**
 *
 * @author Sukhraj
 */
@Document(collection = "forms")
public class FormEntityMongo implements FormEntity {

    private UUID id;
    private String name;
    private String schema;
    private String applicationName;
    private String businessStream;
    private String description;
    private Instant validFrom;
    private Instant validTo;
    
    @SuppressWarnings("UnusedDeclaration") // For Jackson
    protected FormEntityMongo() {
    }
    
    public FormEntityMongo(Form vo) {
        id = UUID.randomUUID();
        name = vo.getName();
        schema = vo.getSchema();
        applicationName = vo.getApplicationName();
        businessStream = vo.getBusinessStream();
        description = vo.getDescription();
        validFrom = vo.getValidFrom();
        validTo = vo.getValidTo();
    }
    
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBusinessStream() {
        return businessStream;
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
