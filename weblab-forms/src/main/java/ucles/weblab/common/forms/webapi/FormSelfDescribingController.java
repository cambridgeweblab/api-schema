package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMethod;
import ucles.weblab.common.identity.domain.Belongs;
import ucles.weblab.common.schema.webapi.ControllerMethodSchemaCreator;
import ucles.weblab.common.schema.webapi.SchemaProvidingController;
import ucles.weblab.common.webapi.HateoasUtils;
import ucles.weblab.common.webapi.LinkRelation;
import ucles.weblab.common.webapi.resource.ResourceListWrapper;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 *
 * @author Sukhraj
 */
public abstract class FormSelfDescribingController<C extends FormSelfDescribingController<C, R>, R>
            extends SchemaProvidingController<C> {

    private final Class<R> resourceClass;
    private final ControllerMethodSchemaCreator controllerMethodSchemaCreator;

    public FormSelfDescribingController(ControllerMethodSchemaCreator controllerMethodSchemaCreator) {
        this.resourceClass = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.controllerMethodSchemaCreator = controllerMethodSchemaCreator;
    }

    @Override
    public ResponseEntity<JsonSchema> describe(Belongs principal) {
        return describe(principal, null, null);
    }

    public ResponseEntity<JsonSchema> describe(@AuthenticationPrincipal Belongs principal, String businessName, String applicationName) {
        // The ResourceSchemaCreator will only add these methods if you're permitted to access them, so we can pass them

        Object controllerListMethod = self().list(null, null);
        JsonSchema instancesSchema = controllerMethodSchemaCreator.createForRequestParams(controllerListMethod);

        LinkDescriptionObject instancesLink = new LinkDescriptionObject()
                .setRel(LinkRelation.INSTANCES.rel())
                .setMethod(RequestMethod.GET.toString())
                .setHref(HateoasUtils.toUriString(linkTo(controllerListMethod), false))
                .setSchema(instancesSchema);

        JsonSchema schema = getSchemaCreator().create(resourceClass,
                self().describe(principal),
                Optional.of(instancesLink),
                Optional.of(self().create(null)));

        return ResponseEntity.ok(schema);
    }

    abstract public ResourceListWrapper<R> list(String businessName, String applicationName);
    abstract public ResponseEntity<R> create(R data);


}
