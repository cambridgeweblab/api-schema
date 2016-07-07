package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.schema.webapi.ControllerMethodSchemaCreator;
import ucles.weblab.common.schema.webapi.JsonSchemaMetadata;
import ucles.weblab.common.schema.webapi.SchemaMediaTypes;
import ucles.weblab.common.webapi.resource.ResourceListWrapper;
import ucles.weblab.common.xc.service.CrossContextMapping;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static ucles.weblab.common.webapi.HateoasUtils.locationHeader;
import static ucles.weblab.common.webapi.MoreMediaTypes.APPLICATION_JSON_UTF8_VALUE;

/**
 *
 * @author Sukhraj
 */
@RestController
@RequestMapping("/api/forms")
public class FormController extends FormSelfDescribingController<FormController, FormResource> {
    
    private static final Logger log = LoggerFactory.getLogger(FormController.class);
    
    private final FormDelegate formDelegate;

    @Autowired
    public FormController(FormDelegate formDelegate, 
                          ControllerMethodSchemaCreator controllerMethodSchemaCreator) {
        
        super(controllerMethodSchemaCreator);
        this.formDelegate = formDelegate;
    }
    
    @RequestMapping(value = "/", 
                    method = RequestMethod.POST, 
                    consumes = APPLICATION_JSON_VALUE, 
                    produces = APPLICATION_JSON_UTF8_VALUE)
    @Override
    public ResponseEntity<FormResource> create(@Valid @RequestBody FormResource formResource) {
        
        FormResource created = formDelegate.create(formResource);
        
        return new ResponseEntity<>(created, locationHeader(created), HttpStatus.CREATED);
    }
        
    
    @RequestMapping(value = "/", 
                    method = GET, 
                    produces = APPLICATION_JSON_UTF8_VALUE)   
    @Override
    public ResourceListWrapper<FormResource> list(@JsonSchemaMetadata(title = "Business Stream", order = 40)
                                                  @RequestParam(value = "businessStream", required=false) String businessStream,
                                                  @JsonSchemaMetadata(title = "Application Name", order = 40)
                                                  @RequestParam(value = "applicationName", required=false) String applicationName  ) {
        
        List<FormResource> result = formDelegate.list(businessStream, applicationName);
        ResourceListWrapper<FormResource> list = ResourceListWrapper.wrap(result);
        
        return list;
    }        
    
    @RequestMapping(value = "/{id}",
            method = GET,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<? extends FormResource> view(@Valid @PathVariable String id) {
        
        FormResource formResource = formDelegate.get(id);
        addDescribedByLink(formResource);
        return new ResponseEntity<>(formResource, locationHeader(formResource), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/",
            method = PUT,
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<? extends FormResource> update(@Valid @RequestBody FormResource formResource) {
        
        FormResource result = formDelegate.update(formResource);
        addDescribedByLink(result);
        return new ResponseEntity<>(result, locationHeader(result), HttpStatus.OK);

    }
    
    @CrossContextMapping(value = "urn:xc:form:businessstreams")
    @RequestMapping(value = "/$businessstreams",
            method = GET,
            produces = SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE)
    public ResponseEntity<JsonSchema> businessstreams() {
        
        List<BusinessStreamBean> businessStreams = Arrays.asList(new BusinessStreamBean("CIE", "Cambridge International Examinations"), 
                                                             new BusinessStreamBean("OCR", "Oxford, Cambridge and RSA"),
                                                             new BusinessStreamBean("CE", "Cambridge English Language Assessment"));
        final JsonSchema enumSchema = this.getSchemaCreator().createEnum(businessStreams, 
                                                                        methodOn(FormController.class).businessstreams(),
                                                                        BusinessStreamBean::getAbbreviation, 
                                                                        Optional.of(BusinessStreamBean::getName));
        return ResponseEntity.ok(enumSchema);
    }
}
