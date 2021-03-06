package ucles.weblab.common.schema.webapi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata for a schema type.
 * @since 05/10/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface JsonSchemaMetadata {

    int MAX_ORDER = 999;

    /**
     * A title to output with the schema for this type.
     */
    String title() default "";

    /**
     * A description to output with the schema for this type.
     */
    String description() default "";

    /**
     * If set, this is used as a message key to look up a tranlation according to the current {@link java.util.Locale}.
     * The result will then override the <code>title</code> property
     */
    String titleKey() default "";

    /**
     * If set, this is used as a message key to look up a tranlation according to the current {@link java.util.Locale}.
     * The result will then override the <code>description</code> property
     */
    String descriptionKey() default "";

    /**
     * The default value for this field.
     */
    String defaultValue() default "";

    /**
     * The logical order of this field within the schema.
     * The properties in the final schema will be output in this order.
     * Any fields without an {@code order} property will be output last, in alphabetical order.
     */
    int order() default MAX_ORDER;
}
