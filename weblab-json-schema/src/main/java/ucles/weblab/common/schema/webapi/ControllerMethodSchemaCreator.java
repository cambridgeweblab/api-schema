package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static ucles.weblab.common.webapi.HateoasUtils.toUriString;

/**
 * Creates a <a href='http://tools.ietf.org/html/draft-zyp-json-schema-03'>(draft-3) JSON Schema</a>
 * for a given controller method's parameters.
 */
public class ControllerMethodSchemaCreator {
    private final ObjectMapper objectMapper;
    private final CrossContextConversionService crossContextConversionService;
    private final EnumSchemaCreator enumSchemaCreator;

    public ControllerMethodSchemaCreator(ObjectMapper objectMapper, CrossContextConversionService crossContextConversionService, EnumSchemaCreator enumSchemaCreator) {
        this.objectMapper = objectMapper;
        this.crossContextConversionService = crossContextConversionService;
        this.enumSchemaCreator = enumSchemaCreator;
    }

    /**
     * Creates a (serializable) JSON schema object describing the request parameters to a controller method.
     * The parameters can be annotated with {@code javax.validation} constraints as well as {@link JsonProperty @JsonProperty},
     * {@link JsonPropertyDescription @JsonPropertyDescription},
     * {@link JsonSchema @JsonSchema} and {@link JsonSchemaMetadata @JsonSchemaMetadata} to enrich the content of this schema
     * object, otherwise it will simply be parameter names and types.
     * <p>
     *     The {@code Object} method reference passed here is a {@link org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware}
     *     object obtained from Spring HATEOAS with {@link ControllerLinkBuilder#methodOn(Class, Object...)}. The
     *     {@code controllerMethod} will be used as the ID of the schema itself.
     * </p>
     * <p>
     *     The schma returned is in the <a href='http://tools.ietf.org/html/draft-zyp-json-schema-03'>JSON Schema draft-3</a> model.
     * </p>
     * @param controllerMethod a {@code LastInvocationAware} method reference to the controller method with all parameters populated
     * @return a schema describing the request parameters, which can be serialized to JSON itself.
     */
    public com.fasterxml.jackson.module.jsonSchema.JsonSchema createForRequestParams(Object controllerMethod) {
        Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, controllerMethod);
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        Parameter[] parameters = ((DummyInvocationUtils.LastInvocationAware) controllerMethod).getLastInvocation().getMethod().getParameters();
        SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
        DefaultSerializerProvider.Impl serializerProvider = ((DefaultSerializerProvider.Impl) objectMapper.getSerializerProvider()).createInstance(serializationConfig, objectMapper.getSerializerFactory());
        // Create a base object schema
        SuperSchemaFactoryWrapper objectWrapper = new SuperSchemaFactoryWrapper(crossContextConversionService, enumSchemaCreator, objectMapper, evalContext);
        SuperSchemaFactoryWrapper.ObjectVisitorDecorator propertyEnhancer = (SuperSchemaFactoryWrapper.ObjectVisitorDecorator) objectWrapper.expectObjectFormat(TypeFactory.unknownType());
        ObjectSchema objectSchema = objectWrapper.finalSchema().asObjectSchema();

        // Create schemas for each request parameter (as properties) and add them to the object schema
        try {
            SchemaFactoryWrapper propertyWrapper = new SuperSchemaFactoryWrapper(crossContextConversionService, enumSchemaCreator, objectMapper, evalContext);
            for (Parameter parameter : parameters) {
                if (parameter.getAnnotation(RequestParam.class) != null) {
                    JavaType javaType = serializationConfig.constructType(parameter.getType());
                    BeanProperty beanProperty = beanPropertyForParameter(parameter, javaType);
                    JsonSerializer<?> serializer = serializerProvider.findTypedValueSerializer(javaType, false, beanProperty);
                    serializer.acceptJsonFormatVisitor(propertyWrapper, javaType);
                    PropertyMetadata propertyMetadata = beanProperty.getMetadata();
                    if (propertyMetadata.isRequired()) {
                        objectSchema.putProperty(beanProperty, propertyWrapper.finalSchema());
                    } else {
                        objectSchema.putOptionalProperty(beanProperty, propertyWrapper.finalSchema());
                    }
                    propertyEnhancer.fixupDateTimeSchema(beanProperty);
                    propertyEnhancer.processValidationConstraints(beanProperty);
                    if (propertyMetadata.hasDefaultValue() && propertyWrapper.finalSchema().asSimpleTypeSchema().getDefault() == null) {
                        propertyWrapper.finalSchema().asSimpleTypeSchema().setDefault(propertyMetadata.getDefaultValue());
                    }
                }
            }

            objectSchema.setId(toUriString(linkTo(controllerMethod), false));
            return objectSchema;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fakes out just enough of a {@link BeanProperty} for a method parameter.
     * The parameter can then be treated as if it were a property of an object for the purposes of schema generation.
     *
     * @param parameter the parameter in question
     * @param javaType the Jackson type of the parameter (from {@link com.fasterxml.jackson.databind.SerializationConfig#constructType(Class)}.
     * @return a bean property instance
     */
    private BeanProperty beanPropertyForParameter(Parameter parameter, final JavaType javaType) {
        return ParameterToBeanProperty.INSTANCE.apply(parameter, javaType);
    }

    private static class ParameterToBeanProperty implements BiFunction<Parameter, JavaType, BeanProperty> {
        private static final ParameterToBeanProperty INSTANCE = new ParameterToBeanProperty();

        @Override
        public BeanProperty apply(Parameter parameter, JavaType javaType) {
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            JsonProperty jsonProperty = parameter.getAnnotation(JsonProperty.class);
            JsonPropertyDescription jsonPropertyDescription = parameter.getAnnotation(JsonPropertyDescription.class);

            String name = requestParam != null? (!StringUtils.isEmpty(requestParam.value())? requestParam.value() : requestParam.name()) : parameter.getName();
            boolean required = isRequired(requestParam, jsonProperty);
            String description = getDescription(jsonPropertyDescription);
            Integer index = getIndex(jsonProperty);
            String defaultValue = getDefaultValue(requestParam, jsonProperty);
            PropertyMetadata propertyMetadata = PropertyMetadata.construct(required, description, index, defaultValue);

            return (BeanProperty) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{BeanProperty.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getAnnotation":
                        return parameter.getAnnotation((Class<Annotation>) args[0]);
                    case "getType":
                        return javaType;
                    case "getName":
                        return name;
                    case "getMetadata":
                        return propertyMetadata;
                    case "getMember":
                        return null;
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });
        }

        private boolean isRequired(RequestParam requestParam, JsonProperty jsonProperty) {
            return (jsonProperty != null && jsonProperty.required())
                    || (requestParam != null && requestParam.required() && requestParam.defaultValue().equals(ValueConstants.DEFAULT_NONE));
        }

        private String getDescription(JsonPropertyDescription jsonPropertyDescription) {
            if (jsonPropertyDescription != null) {
                return jsonPropertyDescription.value();
            }
            return null;
        }

        private Integer getIndex(JsonProperty jsonProperty) {
            if (jsonProperty != null && jsonProperty.index() != JsonProperty.INDEX_UNKNOWN) {
                return jsonProperty.index();
            }
            return null;
        }

        private String getDefaultValue(RequestParam requestParam, JsonProperty jsonProperty) {
            if (jsonProperty != null && !jsonProperty.defaultValue().isEmpty()) {
                return jsonProperty.defaultValue();
            } else if (requestParam != null && !requestParam.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
                return requestParam.defaultValue();
            }
            return null;
        }
    }
}
