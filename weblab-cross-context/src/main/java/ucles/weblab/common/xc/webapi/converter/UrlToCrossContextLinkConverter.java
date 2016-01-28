package ucles.weblab.common.xc.webapi.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.springframework.core.convert.converter.Converter;
import ucles.weblab.common.xc.domain.CrossContextLink;

import java.net.URI;

/**
 * @since 29/01/2016
 */
public class UrlToCrossContextLinkConverter extends StdConverter<URI, CrossContextLink> implements Converter<URI, CrossContextLink> {
    public static final UrlToCrossContextLinkConverter INSTANCE = new UrlToCrossContextLinkConverter();

    @Override
    public CrossContextLink convert(URI source) {
        return new CrossContextLink(source);
    }
}
