package ucles.weblab.common.schema.webapi;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Shorthand for GET mapping which produces JSON Schema.
 *
 * Derived from spring-composed
 * @author Sam Brannen (see See https://github.com/sbrannen/spring-composed/tree/master/src/main/java/org/springframework/composed/web/rest)
 */
@RequestMapping(method = GET, produces = SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE)
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface GetJsonSchema {

    @AliasFor(annotation = RequestMapping.class, attribute = "name")
    String name() default "";

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "params")
    String[] params() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "headers")
    String[] headers() default {};

}