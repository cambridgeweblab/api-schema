package ucles.weblab.common.forms.webapi;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ucles.weblab.common.webapi.HateoasUtils.locationHeader;

/**
 *
 * @author Sukhraj
 */
@RestController
@RequestMapping("/api/forms")
public class FormController {
    
    private final FormDelegate formDelegate;
    
    @Autowired
    public FormController(FormDelegate formDelegate) {
        this.formDelegate = formDelegate;
    }
    
    @RequestMapping(value = "/", 
                    method = RequestMethod.POST, 
                    consumes = APPLICATION_JSON_VALUE, 
                    produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<FormResource> save(@RequestBody FormResource formResource) {
        
        FormResource created = formDelegate.create(formResource);
        
        return new ResponseEntity<>(created, locationHeader(created), HttpStatus.CREATED);
    }
    
}
