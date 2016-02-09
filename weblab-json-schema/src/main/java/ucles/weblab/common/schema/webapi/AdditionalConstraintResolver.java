package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.validation.ValidationConstraintResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
class AdditionalConstraintResolver {
    private Logger log = LoggerFactory.getLogger(getClass());

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
                .map(an -> {
                    if (!an.format().isEmpty()) {
                        return JsonValueFormat.valueOf(an.format());
                    } else {
                        return null;
                    }
                });
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

                            StandardEvaluationContext evalContext = new StandardEvaluationContext();
                            Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                                    .map(Authentication::getPrincipal)
                                    .ifPresent(currentUser -> evalContext.setVariable("currentUser", currentUser));
                            String value = expression.getValue(evalContext, String.class);
                            if (value != null) {
                                return URI.create(value);
                            } else {
                                return null;
                            }
                        } catch (ExpressionException e) {
                            log.warn("Ignoring unprocessable enumRef expression: " + an.enumRef(), e);
                        }
                    }
                    return null;
                });
    }
}
