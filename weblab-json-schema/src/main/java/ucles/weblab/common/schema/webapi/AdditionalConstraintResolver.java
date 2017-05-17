package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.validation.ValidationConstraintResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * Catches the stuff that {@link ValidationConstraintResolver} doesn't.
 * In particular, it doesn't distringuish between inclusive and exclusive numeric constraints.
 */
@SuppressWarnings("WeakerAccess")
class AdditionalConstraintResolver {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final StandardEvaluationContext evalContext;

    public AdditionalConstraintResolver(StandardEvaluationContext evalContext) {
        this.evalContext = evalContext;
    }

    public Optional<Boolean> getNumberExclusiveMinimum(BeanProperty prop) {
        DecimalMin decimalMinAnnotation = prop.getAnnotation(DecimalMin.class);
        return Optional.ofNullable(decimalMinAnnotation).map(an -> !an.inclusive());
    }

    public Optional<Boolean> getNumberExclusiveMaximum(BeanProperty prop) {
        DecimalMax decimalMaxAnnotation = prop.getAnnotation(DecimalMax.class);
        return Optional.ofNullable(decimalMaxAnnotation).map(an -> !an.inclusive());
    }

    public Optional<JsonValueFormat> getValueFormat(final BeanProperty prop) {
        JsonSchema jsonSchemaAnnotation = prop.getAnnotation(JsonSchema.class);
        return Optional.ofNullable(jsonSchemaAnnotation)
                .map(an -> an.format().isEmpty() ? null : JsonValueFormat.valueOf(an.format()));
    }

    public Optional<Boolean> getReadOnly(BeanProperty prop) {
        JsonSchema jsonSchemaAnnotation = prop.getAnnotation(JsonSchema.class);
        return Optional.ofNullable(jsonSchemaAnnotation).map(an -> an.readOnly() ? Boolean.TRUE : null);
    }

    public Optional<Boolean> getNotNull(BeanProperty prop) {
        NotNull notNullAnnotation = prop.getAnnotation(NotNull.class);
        return Optional.ofNullable(notNullAnnotation).map(n -> true);
    }

    public Optional<String> getPattern(BeanProperty prop) {
        JsonSchema jsonSchemaAnnotation = prop.getAnnotation(JsonSchema.class);
        return Optional.ofNullable(jsonSchemaAnnotation).map(JsonSchema::pattern).filter(p -> !p.isEmpty());
    }

    public Optional<Map<String, String>> getEnumConstants(BeanProperty prop) {
        JsonSchema jsonSchemaAnnotation = prop.getAnnotation(JsonSchema.class);
        return Optional.ofNullable(jsonSchemaAnnotation)
                .map(an -> {
                    if (an.enumValues().length > 0) {
                        return Arrays.stream(an.enumValues())
                                .collect(Collectors.toMap(EnumConstant::value,
                                        ev -> (ev.title().isEmpty()? ev.value() : ev.title()),
                                        (a,b) -> a, LinkedHashMap::new));
                    } else {
                        return null;
                    }
                });
    }

    public Optional<URI> getEnumRef(BeanProperty prop) {
        JsonSchema jsonSchemaAnnotation = prop.getAnnotation(JsonSchema.class);
        return Optional.ofNullable(jsonSchemaAnnotation)
                .map(an -> {
                    if (!an.enumRef().isEmpty()) {
                        try {
                            final Expression expression = new SpelExpressionParser().parseExpression(an.enumRef(), new TemplateParserContext());
                            String value = expression.getValue(evalContext, String.class);
                            return value == null ? null : URI.create(value);
                        } catch (ExpressionException e) {
                            log.warn("Ignoring unprocessable enumRef expression: " + an.enumRef(), e);
                        }
                    }
                    return null;
                });
    }

    /**
     * Evaluate the read only expression and return an Optional boolean of the result.
     */
    public Optional<Boolean> getReadOnlyExpression(BeanProperty prop) {
        JsonSchema jsonSchemaAnnotation = prop.getAnnotation(JsonSchema.class);
        return Optional.ofNullable(jsonSchemaAnnotation)
                .map(an -> {
                    if (!an.readOnlyExpression().isEmpty()) {
                        try {
                            final Expression expression = new SpelExpressionParser().parseExpression(an.readOnlyExpression(), new TemplateParserContext());

                            return expression.getValue(evalContext, Boolean.class);
                        } catch (ExpressionException e) {
                            log.warn("Ignoring unprocessable readOnlyExpression: " + an.readOnlyExpression(), e);
                        }
                    }
                    return null;
                });
    }

    /**
     * Gets the media type from the annotation
     */
    public Optional<String> getMediaType(BeanProperty prop) {
        JsonSchema jsonSchemaAnnotation = prop.getAnnotation(JsonSchema.class);

        return Optional.ofNullable(jsonSchemaAnnotation)
                .map(an -> an.mediaType().isEmpty() ? null : jsonSchemaAnnotation.mediaType());
    }
}
