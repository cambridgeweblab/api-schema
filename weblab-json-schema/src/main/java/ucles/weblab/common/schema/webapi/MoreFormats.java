package ucles.weblab.common.schema.webapi;

/**
 * This class holds constants for custom formats, in addition to the standard formats in
 * {@link com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat}.
 *
 * @since 22/10/15
 */
public class MoreFormats {
    /**
     * This should be an precise decimal value.
     */
    public static final String CURRENCY = "currency";
    /**
     * Data that is naturally presented in a list. In order to do this, an {@link JsonSchema#enumRef() enumRef} will need to be provided too.
     */
    public static final String LIST = "list";
    /**
     * Data that is naturally presented in a text area. This could apply to an array of strings (where each string is on a new line) or a multi-line string.
     */
    public static final String TEXTAREA = "textarea";
    /**
   	 * A date in ISO 8601 format of YYYY-MM-DDThh:mm:ss (no timezone).  This is the recommended form of date/timestamp.
   	 */
    public static final String LOCAL_DATE_TIME = "local-date-time";

    /**
     * A date in ISO 8601 format of YYYY-MM-DDThh:mm:ss (no timezone).
     */
    public static final String BIRTH_DATE_TIME = "birth-date";

    /**
     * An ISO 3166-1 alpha-2 country code. A UI would probably present this as a list of known countries.
     */
    public static final String COUNTRY = "country";
    /**
     * Data that is naturally tabular. A UI would probably present give the user the opportunity to upload this as CSV as well as entering it interactively.
     * This would only really apply to an array of object types.
     */
    public static final String TABLE = "table";

    /**
     * An e-mail address which requires double-entry validation to be sure it's correct.
     */
    public static final String CONFIRMED_EMAIL = "confirm-email";

    /**
     * The current view context of the UI. This is filled by the UI with some information to identify the currently displayed view.
     */
    public static final String CURRENT_VIEW_CONTEXT = "current-view-context";

    /**
     * Data that represents a rating of some sort. A UI may decide to present this e.g. as a star graphic.
     * Additional constraints on the minimum and maximum values should be present otherwise a UI will have to make assumptions.
     */
    public static final String RATING = "rating";

    /**
     * A universally unique identifier. This would not normally be entered but instead generated if a new one is required.
     */
    public static final String UUID = "uuid";

    private MoreFormats() {
        // prevent instantiation
    }
}
