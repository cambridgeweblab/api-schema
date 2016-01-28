package ucles.weblab.common.xc.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import ucles.weblab.common.xc.service.CrossContextConversionService;
import ucles.weblab.common.xc.webapi.converter.CrossContextLinkToUrlConverter;
import ucles.weblab.common.xc.webapi.converter.UrlToCrossContextLinkConverter;

import java.io.Serializable;
import java.net.URI;

import static ucles.weblab.common.domain.ConfigurableEntitySupport.configureBean;

/**
 * Value object which can be used in entities to persist links between contexts.
 *
 * @since 06/10/15
 */
@Configurable
@JsonSerialize(converter = CrossContextLinkToUrlConverter.class)
@JsonDeserialize(converter = UrlToCrossContextLinkConverter.class)
public class CrossContextLink implements Serializable {
    private final URI uri;

    {
        configureBean(this);
    }

    public Object readResolve() {
        configureBean(this);
        return this;
    }

    @Autowired
    private transient CrossContextConversionService conversionService;

    public CrossContextLink(URI uri) {
        this.uri = uri;
    }

    @Autowired
    void configureCrossContextConversionService(CrossContextConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public URI asUrn() {
        return conversionService.asUrn(uri);
    }

    public URI asUrl() {
        return conversionService.asUrl(uri);
    }
}
