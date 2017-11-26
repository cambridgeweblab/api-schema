package ucles.weblab.common.schema.webapi;

import org.springframework.http.MediaType;

/**
 * Media types relating to JSON Schema.
 *
 * @since 07/10/15
 */
public final class SchemaMediaTypes {
    public static final String APPLICATION_SCHEMA_JSON_UTF8_VALUE = "application/schema+json;charset=UTF-8";
    public static final MediaType APPLICATION_SCHEMA_JSON_UTF8 = MediaType.valueOf(APPLICATION_SCHEMA_JSON_UTF8_VALUE);

    private SchemaMediaTypes() {
        // Prevent instantiation
    }
}
