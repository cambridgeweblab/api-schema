package ucles.weblab.common.schema.webapi;

/**
 * Collects standard validation patterns into one place.
 *
 * @since 09/10/15
 */
public class Patterns {
    /** Pragmatic partial-RFC5322 pattern from http://www.regular-expressions.info/email.html. Should be used case-insensitive. */
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
            "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$";
    /** 1-4 digit country code without '+' prefix, then a space and at least 5 other characters (Solomon & Cook Islands have 5 digit national numbers). */
    public static final String PHONE_PATTERN = "^\\d{1,4} +.{5,}$";
    /** IBAN is two letter country code plus two digit checksum plus up to 28 more alphanumeric chars, which might be grouped into 4s. */
    public static final String IBAN_PATTERN = "^[A-Z]{2}[0-9]{2}(?: ?[0-9A-Z]{4}){2,6}? ?[0-9A-Z]{0,4}$";
    public static final String TIME_PATTERN = "^([01][0-9]|2[0-3]):[0-5][0-9]$";
    public static final String GENDER_PATTERN = "^[MF]$";
    /** Validates that a password includes letters, digits and other characters, and is at least 8 characters long. */
    public static final String PASSWORD_PATTERN = "^(?=.*\\W)(?=.*\\d)(?=.*[a-zA-Z]).{8,}$";

    private Patterns() {
        // prevent instantiation
    }
}
