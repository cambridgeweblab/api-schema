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
@Target({ElementType.METHOD, ElementType.FIELD})
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
