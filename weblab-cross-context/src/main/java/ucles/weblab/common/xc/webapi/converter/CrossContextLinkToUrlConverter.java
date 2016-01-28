package ucles.weblab.common.xc.webapi.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.springframework.core.convert.converter.Converter;
import ucles.weblab.common.xc.domain.CrossContextLink;

import java.net.URI;

/**
 * @since 29/01/2016
 */
public class CrossContextLinkToUrlConverter extends StdConverter<CrossContextLink, URI> implements Converter<CrossContextLink, URI> {
    public static final CrossContextLinkToUrlConverter INSTANCE = new CrossContextLinkToUrlConverter();

    @Override
    public URI convert(CrossContextLink source) {
        return source.asUrl();
    }
}
