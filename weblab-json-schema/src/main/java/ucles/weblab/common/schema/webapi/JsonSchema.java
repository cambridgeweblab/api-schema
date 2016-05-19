package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @since 05/10/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface JsonSchema {
    /**
     * This property defines the type of data, content type, or microformat to
     * be expected in the instance property values. Use values from {@link JsonValueFormat}, {@link MoreFormats}
     * or any other custom value.
     */
    String format() default "";

    /**
     * This property states that the value is read only. A client should not attempt
     * to modify this data before resubmission.
     */
    boolean readOnly() default false;

    /**
     * Defines a fixed set of constant enum values for this property.
     * Only one of {@code enumValues} and {@link #enumRef()} should be specified otherwise behaviour is undefined.
     */
    EnumConstant[] enumValues() default {};

    /**
     * Gives the link to the schema defining a set of enum values for this property.
     * A SpEL expression (<code>#{...}</code>) can be used and the evaluation context will have the following variables set:
     * <dl>
     *     <dt>currentUser</dt><dd>the current security context {@code Authentication.getPrincipal()}</dd>
     * </dl>
     *
     * If the link is a URN, it will be converted using {@link ucles.weblab.common.xc.service.CrossContextConversionService}.
     */
    String enumRef() default "";

    /**
     * Provides a regexp pattern in case we can't use {@link java.util.regex.Pattern @Pattern} (which should be used in preference).
     * {@code @Pattern} can only be used on {@link CharSequence} types, so if we need a pattern on e.g. a date or time, this is
     * the way to do it.
     */
    String pattern() default "";
}
