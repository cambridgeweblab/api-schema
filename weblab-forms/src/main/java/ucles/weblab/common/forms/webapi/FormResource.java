package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.hateoas.ResourceSupport;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;


/**
 * A form resource representing 
 * @author Sukhraj
 */
public class FormResource extends ResourceSupport {
    
    private String name;
    private String applicationName;
    private String businessStream;
    private JsonSchema schema;
    
    /*For hibernate and jackson instantiation*/
    protected FormResource() {
        
    }
    
    public FormResource(String name, String applicationName, String businessStream, JsonSchema schema) {
        this.name = name;
        this.applicationName = applicationName;
        this.businessStream = businessStream;
        this.schema = schema;
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

    public JsonSchema getSchema() {
        return schema;
    }

    
}
