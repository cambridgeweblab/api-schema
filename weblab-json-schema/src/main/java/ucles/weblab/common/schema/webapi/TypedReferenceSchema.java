package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;

/**
 * Extends reference schema with the ability to specify the type (otherwise it defaults to 'object').
 *
 * @since 07/12/15
 */
public class TypedReferenceSchema extends ReferenceSchema {
    private final JsonFormatTypes type;

    public TypedReferenceSchema(String ref, JsonFormatTypes type) {
        super(ref);
        this.type = type;
    }

    @Override
    public JsonFormatTypes getType() {
        return type;
    }
}
