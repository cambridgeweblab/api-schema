
package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import ucles.weblab.common.forms.domain.FormEntity;


/**
 *
 * @author Sukhraj
 */
public class FormResourceAssembler extends ResourceAssemblerSupport<FormEntity, FormResource>{
           
    private static final Logger log = LoggerFactory.getLogger(FormResourceAssembler.class);
        
    private ObjectMapper objectMapper;
    
    public FormResourceAssembler(ObjectMapper objectMapper) {
        super(FormController.class, FormResource.class);
        this.objectMapper = objectMapper;
    }

       
    @Override
    public FormResource toResource(FormEntity entity) {
        FormResource resource = instantiateResource(entity);        
        return resource;
    }
 
    @Override
    protected FormResource instantiateResource(FormEntity entity) {
                               
        JsonNode schemaAsNode = null;
        try {
            schemaAsNode = objectMapper.readTree(entity.getSchema());
        } catch (IOException ex) {
            log.error("Exception converting to jsonode", ex);
        }
        
        FormResource resource = new FormResource(entity.getName(), 
                                                entity.getApplicationName(), 
                                                entity.getBusinessStream(), 
                                                schemaAsNode);
        
        
        return resource;
    }    
    
    
}
