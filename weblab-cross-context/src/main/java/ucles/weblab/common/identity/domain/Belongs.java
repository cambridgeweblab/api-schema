package ucles.weblab.common.identity.domain;

import ucles.weblab.common.xc.domain.CrossContextLink;

/**
 * Although parties are their own domain, we have a need to obtain a party 'key' across the whole system in order
 * to form URLs and check authentication credentials. Party users of different types will need to implement this
 * interface in order to do so. Other entities which belong to something (e.g. a party organisation) could also implement
 * this.
 *
 * @since 07/10/15
 */
public interface Belongs {
    String getOwnerHandle();
    /* These properties were in the ExamPay version, but don't make sense in this. */
    CrossContextLink getOrganisation();

    String getEmailAddress();
    
}
