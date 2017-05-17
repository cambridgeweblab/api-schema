package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static ucles.weblab.common.webapi.LinkRelation.CREATE;
import static ucles.weblab.common.webapi.LinkRelation.INSTANCES;

/**
 * Creates a (draft-3) JSON Schema (http://tools.ietf.org/html/draft-zyp-json-schema-03")
 * for a given resource class.
 *
 * @since 05/10/15
 */
public class ResourceSchemaCreator {
    public static final String HTTP_JSON_SCHEMA_ORG_DRAFT_03_SCHEMA = "http://json-schema.org/draft-03/schema#";
    private final SecurityChecker securityChecker;
    private final ObjectMapper objectMapper;
    private final CrossContextConversionService crossContextConversionService;
    private final EnumSchemaCreator enumSchemaCreator;
    private final JsonSchemaFactory schemaFactory;

    public ResourceSchemaCreator(SecurityChecker securityChecker,
                                ObjectMapper objectMapper,
                                CrossContextConversionService crossContextConversionService,
                                EnumSchemaCreator enumSchemaCreator,
                                JsonSchemaFactory schemaFactory) {
        this.securityChecker = securityChecker;
        this.objectMapper = objectMapper;
        this.crossContextConversionService = crossContextConversionService;
        this.enumSchemaCreator = enumSchemaCreator;
        this.schemaFactory = schemaFactory;
    }

    /**
     * Creates a (serializable) JSON schema object describing a resource. The resource can be annotated with {@code javax.validation}
     * constraints as well as {@link JsonSchema @JsonSchema} and {@link JsonSchemaMetadata @JsonSchemaMetadata}
     * to enrich the content of this schema object, otherwise it will simply be fields and types.
     * <p>
     * The {@code Object} method references passed here are {@link org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware}
     * objects obtained from Spring HATEOAS with {@link ControllerLinkBuilder#methodOn(Class, Object...)}.
     * The {@code listControllerMethod} * and {@code createControllerMethod} are optional, and will be checked via Spring Security to ensure
     * the current user can access them before links are returned as part of the schema. The {@code schemaMethod} will be
     * used as the ID of the schema itself.
     * <p>
     * The schema returned is in the <a href='http://tools.ietf.org/html/draft-zyp-json-schema-03'>JSON Schema draft-3</a> model.
     *
     * @param resourceClass the resource to describe
     * @param schemaMethod a {@code LastInvocationAware} method reference to the schema controller method
     * @param listControllerMethod  a {@code LastInvocationAware} method reference to the controller method to GET a list of instances of these resources
     * @param createControllerMethod a {@code LastInvocationAware} method reference to the controller method to POST a new isntance of this resource
     * @return a schema object describing the resource, which can be serializaed to JSON itself
     */
    public JsonSchema create(Class resourceClass, Object schemaMethod, Optional<Object> listControllerMethod, Optional<Object> createControllerMethod) {
        JsonSchema jsonSchema = createFullSchema(resourceClass, null);
        decorateJsonSchema(jsonSchema, schemaMethod, listControllerMethod, createControllerMethod);

        return jsonSchema;
    }

    /**
     * This will create a schema using an instance instead. The instance will then
     * be available on the Spring context.
     *
     * @param resource - the resource to add on the spring context.
     */
    public JsonSchema create(ResourceSupport resource,
                            Object schemaMethod,
                            Optional<Object> listControllerMethod,
                            Optional<Object> createControllerMethod) {

        JsonSchema jsonSchema = createFullSchema(resource.getClass(), resource);
        decorateJsonSchema(jsonSchema, schemaMethod, listControllerMethod, createControllerMethod);
        return jsonSchema;
    }

    /**
     * As per {@link #create(Class, Object, Optional, Optional)} except that the schema URI is specified explicitly.
     * @param resourceClass the resource to describe
     * @param schemaUri the URI to return as the schema identity
     * @param listControllerMethod  a {@code LastInvocationAware} method reference to the controller method to GET a list of instances of these resources
     * @param createControllerMethod a {@code LastInvocationAware} method reference to the controller method to POST a new isntance of this resource
     * @param <T> the resource type to describe
     * @return a schema object describing the resource, which can be serializaed to JSON itself
     */
    public <T extends Object> JsonSchema create(Class<T> resourceClass, URI schemaUri, Optional<Object> listControllerMethod, Optional<Object> createControllerMethod) {
        JsonSchema jsonSchema = createFullSchema(resourceClass, null);
        decorateJsonSchema(jsonSchema, schemaUri, listControllerMethod, createControllerMethod);

        return jsonSchema;
    }

    public <T extends Object> JsonSchema createEnum(List<T> resources, Object enumMethod, Function<T, String> valueFn, Optional<Function<T, String>> nameFn) {
        final Stream<T> resourceStream = resources.stream();
        return createEnum(resourceStream, enumMethod, valueFn, nameFn, Optional.empty());
    }

    public <T extends Object> JsonSchema createEnum(Stream<T> resourceStream, Object enumMethod, Function<T, String> valueFn, Optional<Function<T, String>> nameFn, Optional<Function<T, String>> descriptionFn) {
        JsonSchema jsonSchema = enumSchemaCreator.createEnum(resourceStream, valueFn, nameFn, descriptionFn, schemaFactory::stringSchema);
        decorateJsonSchema(jsonSchema, enumMethod, Optional.empty(), Optional.empty());
        return jsonSchema;
    }

    /**
     * Sets up a SchemaFactoryWrapper and variables for the spring evaluation context.
     * @param resourceClass - the resource class to make the schema from
     * @param resource - This will be set on the StandardEvaluationContext as 'currentInstance'
     */
    private JsonSchema createFullSchema(Class resourceClass, ResourceSupport resource) {
        try {
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
            if (resource != null) {
                evaluationContext.setVariable("currentInstance", resource);
            }
             Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                        .map(Authentication::getPrincipal)
                        .ifPresent(currentUser -> {
                            evaluationContext.setVariable("currentUser", currentUser);
                        });

            SchemaFactoryWrapper wrapper = new SuperSchemaFactoryWrapper(crossContextConversionService,
                                                                         enumSchemaCreator,
                                                                         objectMapper,
                                                                         evaluationContext);
            objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(resourceClass), wrapper);
            return wrapper.finalSchema();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

    private void decorateJsonSchema(JsonSchema jsonSchema, Object schemaMethod, Optional<Object> listControllerMethod, Optional<Object> createControllerMethod) {
        jsonSchema.setId(ControllerLinkBuilder.linkTo(schemaMethod).toString());
        decorateJsonSchema(jsonSchema, listControllerMethod, createControllerMethod);
    }

    private void decorateJsonSchema(JsonSchema jsonSchema, URI schemaUri, Optional<Object> listControllerMethod, Optional<Object> createControllerMethod) {
        jsonSchema.setId(schemaUri.toString());
        decorateJsonSchema(jsonSchema, listControllerMethod, createControllerMethod);
    }

    private void decorateJsonSchema(JsonSchema jsonSchema, Optional<Object> listControllerMethod, Optional<Object> createControllerMethod) {
        jsonSchema.set$schema(HTTP_JSON_SCHEMA_ORG_DRAFT_03_SCHEMA);

        Optional<LinkDescriptionObject> instances = listControllerMethod
                .map(m -> (m instanceof LinkDescriptionObject)? (LinkDescriptionObject) m : null);
        if (!instances.isPresent()) {
            instances = listControllerMethod
                    .flatMap(this::linkIfPermittedTo)
                    .map(l -> new LinkDescriptionObject().setHref(l.toString()).setRel(INSTANCES.rel()).setMethod(HttpMethod.GET.toString()));
        }

        Optional<LinkDescriptionObject> create = createControllerMethod
                        .map(m -> (m instanceof LinkDescriptionObject) ? (LinkDescriptionObject) m : null);
        if (!create.isPresent()) {
            create = createControllerMethod
                    .flatMap(this::linkIfPermittedTo)
                    .map(l -> new LinkDescriptionObject().setHref(l.toString()).setRel(CREATE.rel()).setMethod(HttpMethod.POST.toString()));
        }

        LinkDescriptionObject[] linkDescriptionObjects = Arrays.asList(instances, create).stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(LinkDescriptionObject[]::new);

        if (linkDescriptionObjects.length > 0) {
            jsonSchema.asSimpleTypeSchema().setLinks(linkDescriptionObjects);
        }
    }

    Optional<ControllerLinkBuilder> linkIfPermittedTo(Object invocationValue) {
        Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, invocationValue);
        DummyInvocationUtils.LastInvocationAware invocations = (DummyInvocationUtils.LastInvocationAware) invocationValue;

        DummyInvocationUtils.MethodInvocation invocation = invocations.getLastInvocation();
        if (securityChecker.check(invocation)) {
            return Optional.of(ControllerLinkBuilder.linkTo(invocationValue));
        } else {
            return Optional.empty();
        }
    }

}
