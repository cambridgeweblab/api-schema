import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaIdResolver;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.UnionTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;

/**
 * Exposes a bug in Jackson relating to {@link JsonSchemaIdResolver#idFromValue(java.lang.Object)} and union types.
 *
 * @since 15/12/15
 */
@Ignore("until this issue is fixed: https://github.com/FasterXML/jackson-module-jsonSchema/issues/90")
public class JacksonUnionTypeSerializationBugTest {
    JsonSchemaFactory jsonSchemaFactory = new JsonSchemaFactory();
    ObjectMapper objectMapper = new ObjectMapper();

    JsonSchema baseEntitySchema;
    JsonSchema valueObjectSchema;

    public void setUpBaseSchemas(final UnionTypeSchema unionTypeSchema) {
        // idSchema allows for String or Integer IDs
        baseEntitySchema = new ObjectSchema();
        JsonSchema idPropertySchema = unionTypeSchema;
        ValueTypeSchema stringIdSchema = jsonSchemaFactory.stringSchema();
        ValueTypeSchema integerIdSchema = jsonSchemaFactory.integerSchema();
        idPropertySchema.asUnionTypeSchema().setElements(new ValueTypeSchema[] { stringIdSchema, integerIdSchema });
        baseEntitySchema.asObjectSchema().setProperties(Collections.singletonMap("id", idPropertySchema));

        // valueObjectSchema contains the value object properties
        valueObjectSchema = new ObjectSchema();
        valueObjectSchema.asObjectSchema().setProperties(new HashMap<String,JsonSchema>() {{
            put("name", jsonSchemaFactory.stringSchema());
            put("age", jsonSchemaFactory.integerSchema());
        }});
    }

    @Test
    public void testCombinedSchemaWithBaseUnionType() throws JsonProcessingException {
        setUpBaseSchemas(new UnionTypeSchema());

        // An entity consists of an ID and value object properties.
        ObjectSchema entitySchema = jsonSchemaFactory.objectSchema();
        entitySchema.setExtends(new JsonSchema[]{baseEntitySchema, valueObjectSchema});

        // This will fail with a NPE
        String json = objectMapper.writeValueAsString(entitySchema);
        System.out.println(json);
    }

    @Test
    public void testCombinedSchemaWithTypedUnionType() throws JsonProcessingException {
        setUpBaseSchemas(new TypedUnionTypeSchema());

        // An entity consists of an ID and value object properties.
        ObjectSchema entitySchema = jsonSchemaFactory.objectSchema();
        entitySchema.setExtends(new JsonSchema[]{baseEntitySchema, valueObjectSchema});

        // This will fail with a NPE
        String json = objectMapper.writeValueAsString(entitySchema);
        System.out.println(json);
        assertFalse("Do not expect an \"elements\" property as it is not defined in JSON Schema 03", json.contains("\"elements\":"));
        assertFalse("Do not expect two \"type\" properties on the same object", json.contains("\"type\":\"any\",\"type\":"));
    }

    @Test
    public void testCombinedSchemaWithPatchedUnionType() throws JsonProcessingException {
        setUpBaseSchemas(new PatchedUnionTypeSchema());

        // An entity consists of an ID and value object properties.
        ObjectSchema entitySchema = jsonSchemaFactory.objectSchema();
        entitySchema.setExtends(new JsonSchema[]{baseEntitySchema, valueObjectSchema});

        // This will fail with a NPE
        String json = objectMapper.writeValueAsString(entitySchema);
        System.out.println(json);
        assertFalse("Do not expect an \"elements\" property as it is not defined in JSON Schema 03", json.contains("\"elements\":"));
        assertFalse("Do not expect two \"type\" properties on the same object", json.contains("\"type\":\"any\",\"type\":"));
    }

    @Test
    public void testCombinedSchemaWithAnnotatedUnionType() throws JsonProcessingException {
        setUpBaseSchemas(new AnnotatedPatchedUnionTypeSchema());

        // An entity consists of an ID and value object properties.
        ObjectSchema entitySchema = jsonSchemaFactory.objectSchema();
        entitySchema.setExtends(new JsonSchema[]{baseEntitySchema, valueObjectSchema});

        // This will fail with a NPE
        String json = objectMapper.writeValueAsString(entitySchema);
        System.out.println(json);
        assertFalse("Do not expect an \"elements\" property as it is not defined in JSON Schema 03", json.contains("\"elements\":"));
        assertFalse("Do not expect two \"type\" properties on the same object", json.contains("\"type\":\"any\",\"type\":"));
    }

    public static class TypedUnionTypeSchema extends UnionTypeSchema {
        @Override
        public JsonFormatTypes getType() {
            return JsonFormatTypes.ANY;
        }
    }

    public static class PatchedUnionTypeSchema extends TypedUnionTypeSchema {
        @Override
        @JsonProperty("type")
        public ValueTypeSchema[] getElements() {
            return super.getElements();
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, property = "type")
    public static class AnnotatedPatchedUnionTypeSchema extends PatchedUnionTypeSchema {
    }
}
