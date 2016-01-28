package ucles.weblab.common.schema.webapi;

import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.function.Function;

/**
 * This class is a mixin for {@link SelfDescribingController} which adds methods to get an enum list.
 *
 * @param <Self>
 * @param <Resource>
 * @since 14/10/15
 */
public abstract class EnumerableListController<Self extends EnumerableListController<Self, Resource>, Resource> extends SelfDescribingController<Self, Resource> {

    protected com.fasterxml.jackson.module.jsonSchema.JsonSchema enumSchema(String owner, Function<Resource, String> valueFn, Optional<Function<Resource, String>> nameFn) {
        return getSchemaCreator().createEnum(list(owner).getList(), self().enumerate(owner), valueFn, nameFn);
    }

    abstract public ResponseEntity<com.fasterxml.jackson.module.jsonSchema.JsonSchema> enumerate(String owner);
}
