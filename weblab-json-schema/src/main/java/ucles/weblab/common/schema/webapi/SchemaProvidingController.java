package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import ucles.weblab.common.identity.domain.Belongs;
import ucles.weblab.common.webapi.LinkRelation;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Base class for controllers which provide a schema on "/$schema".
 *
 * @since 05/01/16
 */
public abstract class SchemaProvidingController<Self extends SchemaProvidingController> {
    ResourceSchemaCreator schemaCreator;

    @Autowired
    void configureSchemaCreator(ResourceSchemaCreator schemaCreator) {
        this.schemaCreator = schemaCreator;
    }

    public ResourceSchemaCreator getSchemaCreator() {
        return schemaCreator;
    }

    @RequestMapping(value = "/$schema", method = GET, produces = SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE)
    public abstract ResponseEntity<JsonSchema> describe(@AuthenticationPrincipal Belongs principal);

    protected void addDescribedByLink(Object resource) {        
        
        if (resource instanceof ResourceSupport) {
            ResourceSupport r = (ResourceSupport)resource;
            r.add(linkTo(self().describe(null)).withRel(LinkRelation.DESCRIBED_BY.rel()));
        }
    }

    public Self self() {
        //noinspection unchecked
        return (Self) methodOn((Class) getClass());
    }
}
