package ucles.weblab.common.schema.webapi;

/**
 * Valid enum value for a field. Title is optional, but if present is displayed in place of the value on the UI.
 *
 * @since 14/12/15
 */
public @interface EnumConstant {
    /**
     * @return the valid enum value
     */
    String value();

    /**
     * @return the display title to output with the schema for this value.
     */
    String title() default "";
}
