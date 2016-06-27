package ucles.weblab.common.forms.webapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.schema.webapi.SelfDescribingController;
import ucles.weblab.common.webapi.resource.ResourceListWrapper;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static ucles.weblab.common.webapi.HateoasUtils.locationHeader;
import static ucles.weblab.common.webapi.MoreMediaTypes.APPLICATION_JSON_UTF8_VALUE;

/**
 *
 * @author Sukhraj
 */
@RestController
@RequestMapping("/api/forms")
public class FormController extends SelfDescribingController<FormController, FormResource> {
    
    private static final Logger log = LoggerFactory.getLogger(FormController.class);
    
    private final FormDelegate formDelegate;
    
    @Autowired
    public FormController(FormDelegate formDelegate) {
        this.formDelegate = formDelegate;
    }
    
    @Override
    @RequestMapping(value = "/{businessStream}/", 
                    method = RequestMethod.POST, 
                    consumes = APPLICATION_JSON_VALUE, 
                    produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<FormResource> create(@Valid @PathVariable String businessStream,
                                               @Valid @RequestBody FormResource formResource) {
        
        FormResource created = formDelegate.create(formResource);
        
        return new ResponseEntity<>(created, locationHeader(created), HttpStatus.CREATED);
    }
      
    @Override
    @RequestMapping(value = "/{businessStream}/", 
                    method = GET, 
                    produces = APPLICATION_JSON_UTF8_VALUE)   
    public ResourceListWrapper<FormResource> list(@Valid @PathVariable String businessStream) {
        List<FormResource> result = new ArrayList<>();
        ResourceListWrapper<FormResource> list = ResourceListWrapper.wrap(result);
        
        return list;
    }        
    
}
