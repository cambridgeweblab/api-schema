package ucles.weblab.common.forms.domain.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;
import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormEntity;

import java.time.Instant;
import java.util.List;
/**
 *
 * @author Sukhraj
 */
@Document(collection = "forms")
public class FormEntityMongo implements FormEntity, Persistable<String>{

    @Id
    private String id;
    
    private String name;
    private String schema;
    private String applicationName;
    private List<String> businessStreams;
    private String description;
    private Instant validFrom;
    private Instant validTo;
    
    @Transient
    private boolean unsaved;
    
    @SuppressWarnings("UnusedDeclaration")
    protected FormEntityMongo() { // For Jackson
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
        unsaved = true;
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
    
    @Override
    public boolean isNew() {
        return unsaved;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public void setBusinessStreams(List<String> businessStreams) {
        this.businessStreams = businessStreams;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }
    
   
    
}
