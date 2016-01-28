package ucles.weblab.common.schema.webapi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as to be ignored when generating a JSON schema for an object.
 *
 * @since 29/01/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface JsonSchemaIgnore {
    boolean value() default true;
}
