package ucles.weblab.common.xc.service;

import java.net.URI;

/**
 * Interface for a class which can convert URIs from URNs (for persistence) to URLs (for APIs).
 * The converters are chained, so the conversion methods return null if this converter cannot
 * help.
 *
 * @since 06/10/15
 */
public interface CrossContextConverter {
    /**
     * Convert the URL (from a Web API) to a URN (for persistence).
     *
     * @param url the URL
     * @return the URN, or null if this converter cannot convert it
     */
    URI toUrn(URI url);

    /**
     * Convert the URN (from a persistent object) to a URL (for a Web API).
     *
     * @param urn the URN
     * @return the URL, or null if this converter cannot convert it
     */
    URI toUrl(URI urn);
}
