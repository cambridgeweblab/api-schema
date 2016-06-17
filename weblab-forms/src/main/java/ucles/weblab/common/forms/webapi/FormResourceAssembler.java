
package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        
    
    public FormResourceAssembler() {
        super(FormController.class, FormResource.class);
    }

       
    @Override
    public FormResource toResource(FormEntity entity) {
        FormResource resource = instantiateResource(entity);        
        return resource;
    }
 
    @Override
    protected FormResource instantiateResource(FormEntity entity) {
                               
        FormResource resource = new FormResource(entity.getName(), 
                                                entity.getApplicationName(), 
                                                entity.getBusinessStream(), 
                                                entity.getSchema());       
        return resource;
    }    
    
    
}
