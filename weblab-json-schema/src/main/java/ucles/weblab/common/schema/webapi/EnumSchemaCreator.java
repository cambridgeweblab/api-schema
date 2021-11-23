package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaIdResolver;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.UnionTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/**
 * Creates a (draft-3) JSON Schema (http://tools.ietf.org/html/draft-zyp-json-schema-03")
 * for an enum, suitable to be used with {@link com.fasterxml.jackson.module.jsonSchema.JsonSchema#setExtends(com.fasterxml.jackson.module.jsonSchema.JsonSchema[])}
 * in order to specify the valid values for any {@link com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema}.
 *
 * @since 14/12/15
 */
public class EnumSchemaCreator {

    /**
     * Creates an enum schema based off a stream source and a set of mapping functions to extract values, names and
     * descriptions from the source objects. If a name or description function is supplied, then the returned schema
     * will be a union schema where each element has exactly one valid enum constant (and the title and/or description).
     * Otherwise, the returned schema will be a simple value type schema.
     *
     * @param sourceStream stream of source objects
     * @param valueFn function to extract the enum constant value from a source object
     * @param nameFn optional function to extract the enum constant title (label) from a source object
     * @param descriptionFn optional function to extract the enum constant description from a source object
     * @param valueSchemaSupplier supplier of new schema objects of the correct type for enum values (e.g. {@link JsonSchemaFactory#stringSchema()}).
     * @param <T> the type of the source object
     * @return the enum schema
     */
    public <T> JsonSchema createEnum(Stream<T> sourceStream, Function<T, String> valueFn, Optional<Function<T, String>> nameFn, Optional<Function<T, String>> descriptionFn, Supplier<ValueTypeSchema> valueSchemaSupplier) {
        JsonSchema jsonSchema;
        if (nameFn.isPresent() || descriptionFn.isPresent()) {
            jsonSchema = new NonBrokenUnionTypeSchema();
            ValueTypeSchema[] elements = sourceStream
                    .map(r -> {
                        final ValueTypeSchema valueSchema = valueSchemaSupplier.get();
                        nameFn.map(f -> f.apply(r)).ifPresent(title -> valueSchema.setTitle(title));
                        descriptionFn.map(f -> f.apply(r)).ifPresent(description -> valueSchema.setDescription(description));
                        valueSchema.setEnums(Collections.singleton(valueFn.apply(r)));
                        return valueSchema;
                    }).toArray(ValueTypeSchema[]::new);
            if (elements.length > 1) {
                jsonSchema.asUnionTypeSchema().setElements(elements);
            } else if (elements.length == 1) {
                return elements[0];
            } else {
                jsonSchema = valueSchemaSupplier.get();
                jsonSchema.setReadonly(true);
                jsonSchema.asValueTypeSchema().setEnums(Collections.emptySet());
            }
        } else {
            jsonSchema = valueSchemaSupplier.get();
            jsonSchema.asValueTypeSchema().setEnums(sourceStream
                    .map(valueFn::apply)
                    .collect(toCollection(LinkedHashSet::new)));
        }

        return jsonSchema;
    }

    /**
     * Creates an enum schema based off a constant map.
     * The map keys are the valid enum constants and the map values are the titles to associate with them.
     * A null value indicates that the title is the same as the enum constant. If the titles are <em>not</em> the same
     * as the constants, then the returned schema will be a union schema where each element has a title and exactly one
     * valid enum constant. Otherwise, the returned schema will be a simple value type schema.
     *
     * @param enumValues map of enum constants to titles
     * @param valueSchemaSupplier supplier of new schema objects of the correct type for enum values (e.g. {@link JsonSchemaFactory#stringSchema()}).
     * @return the enum schema
     */
    public JsonSchema createEnum(Map<String, String> enumValues, Supplier<ValueTypeSchema> valueSchemaSupplier) {
        JsonSchema enumSchema;
        if (enumValues.entrySet().stream().anyMatch(e -> e.getValue() != null && !e.getValue().equals(e.getKey()))) {
            Function<Map.Entry<String, String>, String> valueFn = Map.Entry<String, String>::getKey;
            Optional<Function<Map.Entry<String, String>, String>> nameFn = Optional.of(Map.Entry<String, String>::getValue);
            return createEnum(enumValues.entrySet().stream(), valueFn, nameFn, Optional.empty(), valueSchemaSupplier);
        } else {
            enumSchema = valueSchemaSupplier.get();
            enumSchema.asValueTypeSchema().setEnums(enumValues.keySet());
        }
        return enumSchema;
    }

    /**
     * The Jackson implementation of UnionTypeSchema is completely broken for serialization.
     * It returns a basic type in the 'type' property, and it returns the union in an 'elements' array.
     * The 'type' property should be the array directly.
     * This implementation isn't broken.
     * It also allows you to use any {@link com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema} rather than just value types.
     * TODO: If you include one of these in an 'extends' array, then {@link JsonSchemaIdResolver#idFromValue(java.lang.Object)} kicks in and screws everything up.
     * TODO: Merge these changes back into Jackson proper
     */
    @JsonIgnoreProperties("elements")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, property = "type")
    public static class NonBrokenUnionTypeSchema extends UnionTypeSchema {
        @JsonProperty("type")
        List<SimpleTypeSchema> anyOf = new LinkedList<>();

        @Override
        public JsonFormatTypes getType() {
            // Will be replaced during serialization anyway.
            return JsonFormatTypes.ANY;
        }

        @JsonIgnore
        public List<SimpleTypeSchema> getTypes() {
            return Collections.unmodifiableList(anyOf);
        }

        @JsonIgnore
        public SimpleTypeSchema[] getTypesArray() {
            return anyOf.toArray(new SimpleTypeSchema[anyOf.size()]);
        }

        public void setTypes(SimpleTypeSchema... anyOf) {
            assert anyOf.length >= 2: "Union Type Schemas must contain two or more Simple Type Schemas";
            this.anyOf = Arrays.stream(anyOf).collect(toCollection(LinkedList::new));
        }

        public void addTypes(Stream<SimpleTypeSchema> stream) {
            stream.forEach(anyOf::add);
        }

        /**
         * @deprecated use {@link #getTypes()} instead.
         * @throws ArrayStoreException if the types in this union are not all {@link ValueTypeSchema}.
         */
        @Deprecated
        @Override
        @JsonIgnore
        public ValueTypeSchema[] getElements() {
            return anyOf.toArray(new ValueTypeSchema[anyOf.size()]);
        }

        /**
         * @deprecated use {@link #setTypes(SimpleTypeSchema[])} instead.
         */
        @Deprecated
        @Override
        @JsonIgnore
        public void setElements(ValueTypeSchema[] elements) {
            super.setElements(elements);
            setTypes(elements);
        }

        @Override
        protected boolean _equals(UnionTypeSchema that) {
            if (that instanceof NonBrokenUnionTypeSchema) {
                return arraysEqual(this.getTypesArray(), ((NonBrokenUnionTypeSchema) that).getTypesArray()) &&
                        equals(this.getId(), this.getId()) &&
                        equals(this.getRequired(), that.getRequired()) &&
                        equals(this.getReadonly(), that.getReadonly()) &&
                        equals(this.get$ref(), that.get$ref()) &&
                        equals(this.get$schema(), that.get$schema()) &&
                        arraysEqual(this.getDisallow(), that.getDisallow()) &&
                        arraysEqual(this.getExtends(), that.getExtends());
            } else {
                return arraysEqual(this.getTypesArray(), that.getElements()) && super._equals(that);
            }
        }
    }
}
