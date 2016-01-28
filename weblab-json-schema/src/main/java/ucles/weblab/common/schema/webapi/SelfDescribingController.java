package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import ucles.weblab.common.identity.domain.Belongs;
import ucles.weblab.common.webapi.resource.ResourceListWrapper;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * Base class for controllers which are automatically self-describing (i.e. can serve out their own schema based on a resource).
 *
 * @param <Self> type of controller
 * @param <Resource> type of resource managed by the controller
 * @since 07/10/15
 */
public abstract class SelfDescribingController<Self extends SelfDescribingController<Self, Resource>, Resource>
        extends SchemaProvidingController<Self> {

    private final Class<Resource> resourceClass;
    public SelfDescribingController() {
        this.resourceClass = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    @Override
    public ResponseEntity<JsonSchema> describe(@AuthenticationPrincipal Belongs principal) {
        final Optional<String> ownerHandle = Optional.ofNullable(principal).map(Belongs::getOwnerHandle);

        // The ResourceSchemaCreator will only add these methods if you're permitted to access them, so we can pass them
        JsonSchema schema = schemaCreator.create(resourceClass,
                self().describe(principal),
                ownerHandle.map(centreNumber -> self().list(centreNumber)),
                ownerHandle.map(centreNumber -> self().create(centreNumber, null)));
        return ResponseEntity.ok(schema);
    }

    abstract public ResourceListWrapper<Resource> list(String owner);
    abstract public ResponseEntity<Resource> create(String owner, Resource data);

}
