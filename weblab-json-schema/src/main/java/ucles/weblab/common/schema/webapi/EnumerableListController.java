package ucles.weblab.common.schema.webapi;

import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.function.Function;

/**
 * This class is a mixin for {@link SelfDescribingController} which adds methods to get an enum list.
 *
 * @param <C> This controller class
 * @param <R> The resource class
 * @since 14/10/15
 */
public abstract class EnumerableListController<C extends EnumerableListController<C, R>, R> extends SelfDescribingController<C, R> {

    protected com.fasterxml.jackson.module.jsonSchema.JsonSchema enumSchema(String owner, Function<R, String> valueFn, Optional<Function<R, String>> nameFn) {
        return getSchemaCreator().createEnum(list(owner).getList(), self().enumerate(owner), valueFn, nameFn);
    }

    abstract public ResponseEntity<com.fasterxml.jackson.module.jsonSchema.JsonSchema> enumerate(String owner);
}
