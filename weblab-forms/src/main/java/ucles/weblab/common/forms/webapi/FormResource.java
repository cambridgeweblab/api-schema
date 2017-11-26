package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import org.springframework.hateoas.ResourceSupport;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import ucles.weblab.common.schema.webapi.JsonSchema;
import ucles.weblab.common.schema.webapi.JsonSchemaMetadata;


/**
 * A form resource representing 
 * @author Sukhraj
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormResource extends ResourceSupport {
    
    @JsonSchemaMetadata(title = "ID", description = "Unique identifier of the form, e.g. a short name", order = 0)
    private String formId;
    
    @NotNull
    @JsonSchemaMetadata(title = "Name", description = "Name of the form", order = 1)
    private String name;
    
    @JsonSchemaMetadata(title = "Description", description = "Description of the form", order = 2)
    private String description;
    
    @NotNull
    @JsonSchemaMetadata(title = "Application Name", description = "The application name that this form belong to", order = 3)    
    private String applicationName;
    
    @NotNull
    @JsonSchemaMetadata(title = "Business Streams", description = "Business stream that this form belongs to", order = 4)    
    @JsonSchema(enumRef = "urn:xc:form:businessstreams")
    private List<String> businessStreams;
    
    @NotNull
    @JsonSchemaMetadata(title = "Form Definition", description = "Form definition of the form to save", order = 5)        
    private JsonNode formDefinition;
    
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSchema(format = JsonValueFormat.DATE_TIME_VALUE)
    @JsonSchemaMetadata(title = "Valid from date", description = "Date from which the form is valid", order = 6)    
    private Instant validFrom;
    
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSchema(format = JsonValueFormat.DATE_TIME_VALUE)
    @JsonSchemaMetadata(title = "Valid to date", description = "Date to which the form is valid", order = 7)
    private Instant validTo;
    
    
    protected FormResource() { // For Hibernate, Jackson
        
    }
    
    public FormResource(String formId, 
                        String name,
                        String description,
                        String applicationName, 
                        List<String> businessStreams, 
                        JsonNode formDefinition,
                        Instant validFrom, 
                        Instant validTo) {
        
        this.formId = formId;
        this.name = name;
        this.description = description;
        this.applicationName = applicationName;
        this.businessStreams = businessStreams;
        this.formDefinition = formDefinition;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public String getName() {
        return name;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public List<String> getBusinessStreams() {
        return businessStreams;
    }

    public JsonNode getFormDefinition() {
        return formDefinition;
    }

    public String getDescription() {
        return description;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public String getFormId() {
        return formId;
    }
    
    @Override
    public String toString() {
        return String.format(
                "FormResource[id=%s, name='%s', description='%s', validFrom='%s', validTo='%s']",
                formId, name, description, validFrom, validTo);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final FormResource other = (FormResource) obj;
        return (this.formId == null) ? other.formId == null : this.formId.equals(other.formId);
    }

    @Override
    public int hashCode() {
        int hash = 9;
        hash = 67 * hash + Objects.hashCode(this.formId);
        hash = 67 * hash + Objects.hashCode(this.name);
        return hash;
    }

}
