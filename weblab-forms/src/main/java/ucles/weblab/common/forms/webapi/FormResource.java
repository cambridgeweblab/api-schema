package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import org.springframework.hateoas.ResourceSupport;
import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import ucles.weblab.common.schema.webapi.JsonSchema;
import ucles.weblab.common.schema.webapi.JsonSchemaMetadata;


/**
 * A form resource representing 
 * @author Sukhraj
 */
public class FormResource extends ResourceSupport {
    
    private UUID formId;
    
    @NotNull
    @JsonSchemaMetadata(title = "Name", description = "Name of the form", order = 1)
    private String name;
    
    @JsonSchemaMetadata(title = "Description", description = "Description of the form", order = 2)
    private String description;
    
    @NotNull
    @JsonSchemaMetadata(title = "Application Name", description = "The application name that this form belong to", order = 3)    
    private String applicationName;
    
    @NotNull
    @JsonSchemaMetadata(title = "Business Stream", description = "Business stream that this form belongs to, CIE, OCR or CE", order = 4)    
    private String businessStream;
    
    @NotNull
    @JsonSchemaMetadata(title = "Schema", description = "The schema of the form to save", order = 5)        
    private JsonNode schema;
    
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
    
    
    /*For hibernate and jackson instantiation*/
    protected FormResource() {
        
    }
    
    public FormResource(UUID formId, 
                        String name,
                        String description,
                        String applicationName, 
                        String businessStream, 
                        JsonNode schema,
                        Instant validFrom, 
                        Instant validTo) {
        
        this.formId = formId;
        this.name = name;
        this.description = description;
        this.applicationName = applicationName;
        this.businessStream = businessStream;
        this.schema = schema;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public String getName() {
        return name;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getBusinessStream() {
        return businessStream;
    }

    public JsonNode getSchema() {
        return schema;
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

    public UUID getFormId() {
        return formId;
    }

}
