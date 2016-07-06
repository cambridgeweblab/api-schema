package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import com.fasterxml.jackson.module.jsonSchema.types.UnionTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @since 14/12/15
 */
public class EnumSchemaCreatorTest {
    EnumSchemaCreator enumSchemaCreator = new EnumSchemaCreator();
    JsonSchemaFactory schemaFactory = new JsonSchemaFactory();
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenNoNameFnOrDescriptionFnProvided_whenIntegerSchemaCreated_thenSimpleEnumSchemaCreated() {
        JsonSchema result = enumSchemaCreator.createEnum(IntStream.of(15, 30, 45, 60).boxed(), Object::toString, Optional.empty(), Optional.empty(), schemaFactory::integerSchema);
        assertEquals("Expect integer schema", JsonFormatTypes.INTEGER, result.getType());
        ValueTypeSchema valueTypeSchema = result.asValueTypeSchema();
        assertThat("Expect enum values", valueTypeSchema.getEnums(), Matchers.contains("15", "30", "45", "60"));
    }

    @Test
    public void givenNameFnAndDescriptionFnProvided_whenBooleanSchemaCreated_thenUnionEnumSchemaCreated() {
        Optional<Function<Boolean, String>> nameFn = Optional.of(b -> b ? "♂" : "♀");
        Optional<Function<Boolean, String>> descriptionFn = Optional.of(b -> b ? "Male" : "Female");
        JsonSchema result = enumSchemaCreator.createEnum(Arrays.stream(new Boolean[] { true, false }),
                Object::toString, nameFn, descriptionFn, schemaFactory::booleanSchema);

        UnionTypeSchema unionTypeSchema = result.asUnionTypeSchema();
        assertNotNull("Expect union schema", unionTypeSchema);
        ValueTypeSchema[] elements = unionTypeSchema.getElements();
        assertEquals("Expect one element per enum value", 2, elements.length);
        assertThat("Expect all elements to be boolean schemas", Arrays.asList(elements), Matchers.everyItem(new CustomTypeSafeMatcher<ValueTypeSchema>("has type BOOLEAN") {
            @Override
            protected boolean matchesSafely(ValueTypeSchema item) {
                return item.getType() == JsonFormatTypes.BOOLEAN;
            }
        }));
        List<Set<String>> elementEnums = Arrays.stream(elements).map(ValueTypeSchema::getEnums).collect(Collectors.toList());
        Object[] singletonValueSets = new Object[] { Collections.singleton("true"), Collections.singleton("false") };
        assertThat("Expect enum values", elementEnums, Matchers.contains(singletonValueSets));
        assertThat("Expect enum titles", Arrays.stream(elements).map(ValueTypeSchema::getTitle).collect(Collectors.toList()), Matchers.contains("♂", "♀"));
        assertThat("Expect enum descriptions", Arrays.stream(elements).map(ValueTypeSchema::getDescription).collect(Collectors.toList()), Matchers.contains("Male", "Female"));
    }

    @Test
    public void givenNameFnAndDescriptionFnProvided_whenOnlyOneValue_thenSimpleEnumSchemaCreated() {
        Optional<Function<String, String>> nameFn = Optional.of(b -> "Hobson's Choice");
        Optional<Function<String, String>> descriptionFn = Optional.of(b -> "Take it or leave it");
        JsonSchema result = enumSchemaCreator.createEnum(Stream.of("it"), Function.identity(), nameFn, descriptionFn, schemaFactory::stringSchema);
        assertEquals("Expect string schema", JsonFormatTypes.STRING, result.getType());
        ValueTypeSchema valueTypeSchema = result.asValueTypeSchema();
        assertThat("Expect single string value", valueTypeSchema.getEnums(), Matchers.contains("it"));
        assertEquals("Expect title", "Hobson's Choice", valueTypeSchema.getTitle());
        assertEquals("Expect description", "Take it or leave it", valueTypeSchema.getDescription());
    }

    @Test
    public void givenNameFnAndDescriptionFnProvided_whenNoValues_thenSimpleEnumSchemaCreated() {
        Optional<Function<String, String>> nameFn = Optional.of(b -> "Black Hole");
        Optional<Function<String, String>> descriptionFn = Optional.of(b -> "There's nothing here");
        JsonSchema result = enumSchemaCreator.createEnum(Stream.empty(), Function.identity(), nameFn, descriptionFn, schemaFactory::stringSchema);
        assertEquals("Expect string schema", JsonFormatTypes.STRING, result.getType());
        ValueTypeSchema valueTypeSchema = result.asValueTypeSchema();
        assertThat("Expect no values", valueTypeSchema.getEnums(), Matchers.iterableWithSize(0));
        assertNull("Expect no title", valueTypeSchema.getTitle());
        assertNull("Expect description", valueTypeSchema.getDescription());
    }

    @Test
    public void givenMapValuesMatchKeys_whenStringEnumCreated_thenSimpleEnumSchemaCreated() {
        Map<String,String> enumValues = new LinkedHashMap<String,String>() {{
            put("antelope", "antelope");
            put("barbed", "barbed");
            put("critter", "critter");
        }};
        JsonSchema result = enumSchemaCreator.createEnum(enumValues, schemaFactory::stringSchema);
        assertEquals("Expect string schema", JsonFormatTypes.STRING, result.getType());
        ValueTypeSchema valueTypeSchema = result.asValueTypeSchema();
        assertThat("Expect enum values", valueTypeSchema.getEnums(), Matchers.contains(enumValues.keySet().toArray()));
    }

    @Test
    public void givenMapValuesNotMatchingKeys_whenStringEnumCreated_thenUnionEnumSchemaCreated() throws IOException {
        Map<String,String> enumValues = new LinkedHashMap<String,String>() {{
            put("antelope", "Neotragus pygmaeus");
            put("barbed", "Barbus barbus");
            put("critter", "Critterus Critterii");
        }};
        JsonSchema result = enumSchemaCreator.createEnum(enumValues, schemaFactory::stringSchema);
        UnionTypeSchema unionTypeSchema = result.asUnionTypeSchema();
        assertNotNull("Expect union schema", unionTypeSchema);
        ValueTypeSchema[] elements = unionTypeSchema.getElements();
        assertEquals("Expect one element per enum value", enumValues.size(), elements.length);
        assertThat("Expect all elements to be string schemas", Arrays.asList(elements), Matchers.everyItem(new CustomTypeSafeMatcher<ValueTypeSchema>("has type STRING") {
            @Override
            protected boolean matchesSafely(ValueTypeSchema item) {
                return item.getType() == JsonFormatTypes.STRING;
            }
        }));
        List<Set<String>> elementEnums = Arrays.stream(elements).map(ValueTypeSchema::getEnums).collect(Collectors.toList());
        Object[] singletonValueSets = enumValues.keySet().stream().map(Collections::singleton).toArray();
        assertThat("Expect enum values", elementEnums, Matchers.contains(singletonValueSets));
        assertThat("Expect enum titles", Arrays.stream(elements).map(ValueTypeSchema::getTitle).collect(Collectors.toList()), Matchers.contains(enumValues.values().toArray()));
        assertThat("Expect null enum descriptions", Arrays.stream(elements).map(ValueTypeSchema::getDescription).collect(Collectors.toList()), Matchers.everyItem(Matchers.nullValue()));
    }

    @Test
    public void whenUnionEnumSchemaCreated_thenUnionEnumSchemaHasArrayType() throws IOException {
        Map<String,String> enumValues = new LinkedHashMap<String,String>() {{
            put("antelope", "Neotragus pygmaeus");
            put("barbed", "Barbus barbus");
            put("critter", "Critterus Critterii");
        }};
        JsonSchema result = enumSchemaCreator.createEnum(enumValues, schemaFactory::stringSchema);
        Assume.assumeNotNull("Assume union schema", result.asUnionTypeSchema());
        String json = objectMapper.writeValueAsString(result);
        JsonNode jsonNode = objectMapper.readTree(json);
        assertTrue("Expect union schema", jsonNode.get("type").isArray());
    }

    @Test
    @Ignore("until this issue is fixed: https://github.com/FasterXML/jackson-module-jsonSchema/issues/90")
    public void whenUnionSchemaUsedWithExtends_thenUnionEnumSchemaHasArrayType() throws IOException {
        ObjectSchema schema = schemaFactory.objectSchema();
        StringSchema stringSchema = new StringSchema();
        schema.putProperty("coffee", stringSchema);
        Map<String,String> enumValues = new LinkedHashMap<String,String>() {{
            put("10", "Indriya");
            put("09", "Arpeggio");
        }};
        stringSchema.setExtends(new JsonSchema[]{enumSchemaCreator.createEnum(enumValues, schemaFactory::stringSchema)});
        String json = objectMapper.writeValueAsString(schema);
        assertThat("Expect extends with array type", json, Matchers.containsString("\"extends\":[{\"type\":[{"));
    }
}
