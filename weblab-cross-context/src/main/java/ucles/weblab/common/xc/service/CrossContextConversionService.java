package ucles.weblab.common.xc.service;

import java.net.URI;

/**
 * Interface for service to convert between URNs (for storage) and URLs (for Web APIs).
 *
 * @since 06/10/15
 */
public interface CrossContextConversionService {
    void addConverter(CrossContextConverter converter);
    URI asUrn(URI url);
    URI asUrl(URI urn);
}
