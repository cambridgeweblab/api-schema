package ucles.weblab.common.xc.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service delegating to a list of converters to convert between persistable URNs and APIable URLs.
 * {@link ControllerIntrospectingCrossContextConverter} is added as a default converter, and in the case where
 * all the bounded contexts are hosted on the same server, is all you need.
 *
 * @since 06/10/15
 */
public class CrossContextConversionServiceImpl implements CrossContextConversionService {
    final List<CrossContextConverter> converters = new ArrayList<>();

    public void addConverter(CrossContextConverter converter) {
        this.converters.add(converter);
    }

    public void setConverters(List<CrossContextConverter> converters) {
        this.converters.clear();
        this.converters.addAll(converters);
    }

    @Override
    public URI asUrn(URI url) {
        if (url.isOpaque() && "urn".equalsIgnoreCase(url.getScheme())) {
            // Already a URN.
            return url;
        } else if (url.isAbsolute() && !url.isOpaque()) {
            return converters.stream()
                    .map(xcv -> xcv.toUrn(url))
                    .filter(urn -> urn != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find a converter for the URL: " + url));
        } else {
            throw new IllegalArgumentException("Cannot convert a non-absolute or opaque URL: " + url);
        }
    }

    @Override
    public URI asUrl(URI urn) {
        if (urn.isAbsolute() && !urn.isOpaque()) {
            // Already a URL.
            return urn;
        } else if (urn.isOpaque() && "urn".equalsIgnoreCase(urn.getScheme())) {
            return converters.stream()
                    .map(xcv -> xcv.toUrl(urn))
                    .filter(url -> url != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find a converter for the URN: " + urn));
        } else {
            throw new IllegalArgumentException("Cannot convert an absolute or non-opaque URN: " + urn);
        }
    }
}
