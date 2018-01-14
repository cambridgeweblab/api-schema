package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucles.weblab.common.schema.webapi.ControllerMethodSchemaCreator;
import ucles.weblab.common.schema.webapi.JsonSchemaMetadata;
import ucles.weblab.common.schema.webapi.SchemaMediaTypes;
import ucles.weblab.common.webapi.resource.ResourceListWrapper;
import ucles.weblab.common.xc.service.CrossContextMapping;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static ucles.weblab.common.webapi.HateoasUtils.locationHeader;
import static ucles.weblab.common.webapi.MoreMediaTypes.APPLICATION_JSON_UTF8_VALUE;

/**
 *
 * @author Sukhraj
 */
@RestController
@RequestMapping("/api/forms")
public class FormController extends FormSelfDescribingController<FormController, FormResource> {

    private final FormDelegate formDelegate;
    private final FormSettings formSettings;

    @Autowired
    public FormController(FormDelegate formDelegate,
                          FormSettings formSettings,
                          ControllerMethodSchemaCreator controllerMethodSchemaCreator) {

        super(controllerMethodSchemaCreator);
        this.formDelegate = formDelegate;
        this.formSettings = formSettings;
    }

    @RequestMapping(value = "/",
                    method = POST,
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

    @RequestMapping(value = "/{id}", method = DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        formDelegate.delete(id);
        return ResponseEntity.ok(null);
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
        List<BusinessStreamBean> businessStreams = formSettings.getBusinessStream().entrySet().stream()
                .map(e -> new BusinessStreamBean(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        final JsonSchema enumSchema = this.getSchemaCreator().createEnum(businessStreams,
                                                                        methodOn(FormController.class).businessstreams(),
                                                                        BusinessStreamBean::getAbbreviation,
                                                                        Optional.of(BusinessStreamBean::getName));
        return ResponseEntity.ok(enumSchema);
    }
}
