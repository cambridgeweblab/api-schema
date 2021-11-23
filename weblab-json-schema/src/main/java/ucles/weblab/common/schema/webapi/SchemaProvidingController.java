package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ucles.weblab.common.identity.domain.Belongs;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Base class for controllers which provide a schema on "/$schema/".
 *
 * @since 05/01/16
 */
public abstract class SchemaProvidingController<Self extends SchemaProvidingController<Self>>
        extends SchemaCreatingController<Self> {

    @GetJsonSchema("/$schema/")
    public abstract ResponseEntity<JsonSchema> describe(@AuthenticationPrincipal Belongs principal);

    protected void addDescribedByLink(RepresentationModel<?> resource) {
        resource.add(linkTo(self().describe(null)).withRel(IanaLinkRelations.DESCRIBED_BY));
    }

    /**
     * Convenience method to make {@link org.springframework.hateoas.server.mvc.WebMvcLinkBuilder} expressions more
     * concise e.g.
     * <pre>
     *     linkTo(self().describe())
     * </pre>
     * rather than
     * <pre>
     *     linkTo(methodOn(SomeController.class).describe())
     * </pre>
     *
     * @return {@code methodOn(getClass())}
     */
    public Self self() {
        //noinspection unchecked
        return (Self) methodOn((Class<?>) getClass());
    }

}
