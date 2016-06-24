package ucles.weblab.common.forms.domain.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
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
    
    @SuppressWarnings("UnusedDeclaration") // For Jackson
    protected FormEntityMongo() {
    }
    
    public FormEntityMongo(Form vo) {
        id = UUID.randomUUID();
        name = vo.getName();
        schema = vo.getSchema();
        applicationName = vo.getApplicationName();
        businessStream = vo.getBusinessStream();
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
    
}
