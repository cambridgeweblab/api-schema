package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.validation.AnnotationConstraintResolver;
import com.fasterxml.jackson.module.jsonSchema.validation.ValidationConstraintResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Add a full set of useful hypermedia properties to schema.
 */
public class SuperSchemaFactoryWrapper extends SchemaFactoryWrapper {
    private static Logger log = LoggerFactory.getLogger(SuperSchemaFactoryWrapper.class);

    private final ValidationConstraintResolver constraintResolver = new AnnotationConstraintResolver();
    private final AdditionalConstraintResolver additionalConstraintResolver;
    private final CrossContextConversionService crossContextConversionService;
    private final EnumSchemaCreator enumSchemaCreator;
    private final ObjectMapper objectMapper;
    private final LocalisationService localisationService;

    private static class SuperSchemaFactoryWrapperFactory extends WrapperFactory {
        private final CrossContextConversionService crossContextConversionService;
        private final EnumSchemaCreator enumSchemaCreator;
        private final ObjectMapper objectMapper;
        private final StandardEvaluationContext evaluationContext;
        private final LocalisationService localisationService;

        private SuperSchemaFactoryWrapperFactory(CrossContextConversionService crossContextConversionService,
                                                 EnumSchemaCreator enumSchemaCreator,
                                                 ObjectMapper objectMapper,
                                                 StandardEvaluationContext evaluationContext,
                                                 LocalisationService localisationService) {
            this.crossContextConversionService = crossContextConversionService;
            this.enumSchemaCreator = enumSchemaCreator;
            this.objectMapper = objectMapper;
            this.evaluationContext = evaluationContext;
            this.localisationService = localisationService;
        }

        @Override
        public SchemaFactoryWrapper getWrapper(SerializerProvider p) {
            SchemaFactoryWrapper wrapper = new SuperSchemaFactoryWrapper(crossContextConversionService, enumSchemaCreator, objectMapper, evaluationContext, localisationService);
            wrapper.setProvider(p);
            return wrapper;
        }

        ;

        public SchemaFactoryWrapper getWrapper(SerializerProvider p, VisitorContext rvc) {
            SchemaFactoryWrapper wrapper = new SuperSchemaFactoryWrapper(crossContextConversionService, enumSchemaCreator, objectMapper, evaluationContext, localisationService);
            wrapper.setProvider(p);
            wrapper.setVisitorContext(rvc);
            return wrapper;
        }
    }

    class ObjectVisitorDecorator extends com.fasterxml.jackson.module.jsonSchema.factories.ObjectVisitorDecorator {
        public ObjectVisitorDecorator(final ObjectVisitor objectVisitor) {
            super(objectVisitor);
        }

        private JsonSchema getPropertySchema(BeanProperty writer) {
            return ((ObjectSchema)this.getSchema()).getProperties().get(writer.getName());
        }

        private void setPropertySchema(BeanProperty writer, JsonSchema schema) {
            ((ObjectSchema) this.getSchema()).getProperties().put(writer.getName(), schema);
        }

        private void removePropertySchema(BeanProperty writer) {
            ((ObjectSchema) this.getSchema()).getProperties().remove(writer.getName());
        }

        public void optionalProperty(BeanProperty prop) throws JsonMappingException {
            super.optionalProperty(prop);
            if (!removeLinksAndActions(prop)) {
                fixupDateTimeSchema(prop);
                processValidationConstraints(prop);
            }
        }

        public void property(BeanProperty prop) throws JsonMappingException {
            super.property(prop);
            fixupDateTimeSchema(prop);
            processValidationConstraints(prop);
        }

        void fixupDateTimeSchema(BeanProperty writer) {
            if ((writer.getType().hasRawClass(LocalDateTime.class) ||
                    writer.getType().hasRawClass(LocalTime.class) ||
                    writer.getType().hasRawClass(LocalDate.class) ||
                    writer.getType().hasRawClass(Instant.class))

                    && !(provider.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))) {
                final StringSchema s = schemaProvider.stringSchema();
                this.setPropertySchema(writer, s);
            }
        }

        void processValidationConstraints(BeanProperty prop) {
            JsonSchema existingSchema = this.getPropertySchema(prop);
            JsonSchema updatedSchema = addValidationConstraints(existingSchema, prop);
            if (updatedSchema != existingSchema) {
                this.setPropertySchema(prop, updatedSchema);
            }
        }

        private boolean removeLinksAndActions(BeanProperty writer) {
            if (writer.getType().isCollectionLikeType()) {
                JavaType contentType = writer.getType().getContentType();
                if (contentType.hasRawClass(Link.class) ||
                        (contentType.getRawClass().getAnnotation(JsonSchemaIgnore.class) != null &&
                                contentType.getRawClass().getAnnotation(JsonSchemaIgnore.class).value())) {
                    removePropertySchema(writer);
                    return true;
                }
            }
            return false;
        }
    }

    public SuperSchemaFactoryWrapper(CrossContextConversionService crossContextConversionService,
                                     EnumSchemaCreator enumSchemaCreator,
                                     ObjectMapper objectMapper,
                                     StandardEvaluationContext evaluationContext,
                                     LocalisationService localisationService) {
        super(new SuperSchemaFactoryWrapperFactory(crossContextConversionService, enumSchemaCreator, objectMapper, evaluationContext, localisationService));
        this.crossContextConversionService = crossContextConversionService;
        this.enumSchemaCreator = enumSchemaCreator;
        this.objectMapper = objectMapper;
        this.localisationService = localisationService;
        this.additionalConstraintResolver = new AdditionalConstraintResolver(evaluationContext);
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        return new ObjectVisitorDecorator((ObjectVisitor) super.expectObjectFormat(convertedType));
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType convertedType) {
        return super.expectArrayFormat(convertedType);
    }

    JsonSchema addValidationConstraints(JsonSchema schema, BeanProperty prop) {
        if(schema.isArraySchema()) {
            ArraySchema arraySchema = schema.asArraySchema();
            arraySchema.setMaxItems(constraintResolver.getArrayMaxItems(prop));
            arraySchema.setMinItems(constraintResolver.getArrayMinItems(prop));
            if (arraySchema.getItems().isSingleItems() && arraySchema.getItems().asSingleItems().getSchema().isValueTypeSchema()) {
                ValueTypeSchema itemSchema = arraySchema.getItems().asSingleItems().getSchema().asValueTypeSchema();

                additionalConstraintResolver.getValueFormat(prop).ifPresent(itemSchema::setFormat);
                addEnumConstraints(itemSchema, prop);
            }
        } else if(schema.isNumberSchema()) {
            NumberSchema numberSchema = schema.asNumberSchema();
            numberSchema.setMaximum(constraintResolver.getNumberMaximum(prop));
            numberSchema.setMinimum(constraintResolver.getNumberMinimum(prop));
            additionalConstraintResolver.getNumberExclusiveMaximum(prop).ifPresent(numberSchema::setExclusiveMaximum);
            additionalConstraintResolver.getNumberExclusiveMinimum(prop).ifPresent(numberSchema::setExclusiveMinimum);
        } else if(schema.isStringSchema()) {
            StringSchema stringSchema = schema.asStringSchema();
            stringSchema.setMaxLength(constraintResolver.getStringMaxLength(prop));
            stringSchema.setMinLength(constraintResolver.getStringMinLength(prop));
            stringSchema.setPattern(constraintResolver.getStringPattern(prop));
            if (stringSchema.getPattern() == null) {
                additionalConstraintResolver.getPattern(prop).ifPresent(stringSchema::setPattern);
            }
            additionalConstraintResolver.getMediaType(prop).ifPresent(stringSchema::setMediaType);
        }

        if (schema.isValueTypeSchema()) {
            ValueTypeSchema valueTypeSchema = schema.asValueTypeSchema();
            additionalConstraintResolver.getValueFormat(prop).ifPresent(valueTypeSchema::setFormat);
            addEnumConstraints(valueTypeSchema, prop);
        }

        if (schema.isSimpleTypeSchema()) {
            SimpleTypeSchema simpleTypeSchema = schema.asSimpleTypeSchema();
            final Optional<JsonSchemaMetadata> metadata = Optional.ofNullable(prop.getAnnotation(JsonSchemaMetadata.class));

            metadata.map(JsonSchemaMetadata::title).filter(isNotEmpty()).ifPresent(simpleTypeSchema::setTitle);
            metadata.map(JsonSchemaMetadata::titleKey).filter(isNotEmpty()).ifPresent(key -> localisationService.ifMessagePresent(key, simpleTypeSchema::setTitle));
            metadata.map(JsonSchemaMetadata::description).filter(isNotEmpty()).ifPresent(schema::setDescription);
            metadata.map(JsonSchemaMetadata::descriptionKey).filter(isNotEmpty()).ifPresent(key -> localisationService.ifMessagePresent(key, simpleTypeSchema::setDescription));
            metadata.map(JsonSchemaMetadata::defaultValue).filter(isNotEmpty()).ifPresent(simpleTypeSchema::setDefault);

            // Put the order in the ID so we can post-process the object in {@link #finalSchema} and order the properties.
            final Integer order = metadata.map(JsonSchemaMetadata::order).orElse(JsonSchemaMetadata.MAX_ORDER);
            schema.setId(String.format("order:%03d_%s", order, prop.getName()));
        }

        additionalConstraintResolver.getReadOnly(prop).ifPresent(schema::setReadonly);
        additionalConstraintResolver.getReadOnlyExpression(prop).ifPresent(schema::setReadonly);
        additionalConstraintResolver.getNotNull(prop).ifPresent(schema::setRequired);

        return schema;
    }

    void addEnumConstraints(ValueTypeSchema schema, BeanProperty prop) {
        additionalConstraintResolver.getEnumConstants(prop).ifPresent(enumValues -> {
            JsonSchema enumSchema = enumSchemaCreator.createEnum(enumValues, matchingSchemaType(schema));
            // TODO: Re-inline union schemas when defect fixed - https://github.com/FasterXML/jackson-module-jsonSchema/issues/90
            if (enumSchema.isUnionTypeSchema()) {
                // Switch to a ref schema instead and return the schema as a data URI.
                try {
                    final URI enumRef = URI.create("data:" + SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE + ";base64,"
                            + Base64.getUrlEncoder().encodeToString(objectMapper.writeValueAsBytes(enumSchema)));
                    log.debug("Converting inline union enum schema to data URI: {}", enumRef);

                    schema.setExtends(new JsonSchema[] {
                            new TypedReferenceSchema(enumRef.toString(), schema.getType())
                    });
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                schema.setExtends(new JsonSchema[]{ enumSchema });
            }
        });
        additionalConstraintResolver.getEnumRef(prop).map(crossContextConversionService::asUrl).ifPresent(ref ->
                schema.setExtends(new JsonSchema[]{
                        new TypedReferenceSchema(ref.toString(), schema.getType())
                }));
    }

    public static String encodeURIComponent(String s) throws UnsupportedEncodingException {
        String result;

        result = URLEncoder.encode(s, StandardCharsets.UTF_8.name())
                .replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%7E", "~");

        return result;
    }

    private Supplier<ValueTypeSchema> matchingSchemaType(JsonSchema schema) {
        return () -> {
            try {
                return (ValueTypeSchema) schema.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public JsonSchema finalSchema() {
        final JsonSchema jsonSchema = super.finalSchema();

        if (jsonSchema.isObjectSchema()) {
            // Reorder the properties according to the ID
            final Map<String, JsonSchema> unorderedProperties = jsonSchema.asObjectSchema().getProperties();
            final LinkedHashMap<String, JsonSchema> orderedProperties = unorderedProperties.entrySet().stream()
                    // Sort the properties by ID
                    .sorted(Map.Entry.comparingByValue((a, b) -> a.getId().compareTo(b.getId())))
                            // Collect into a LinkedHashMap to preserve the sorted order.
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
            jsonSchema.asObjectSchema().setProperties(orderedProperties);
        }

        return jsonSchema;
    }

    private Predicate<String> isNotEmpty() {
        return ((Predicate<String>) String::isEmpty).negate();
    }

}
