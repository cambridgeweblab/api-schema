package ucles.weblab.common.identity.domain;

/**
 * Constants for role names. Roles are a cross-context concern so are in the cross-context module.
 */
public final class PartyRoles {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_CENTRE = "ROLE_CENTRE";
    public static final String ROLE_PREP_CENTRE = "ROLE_PREP_CENTRE";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private PartyRoles() { // Prevent instantiation
    }
}
