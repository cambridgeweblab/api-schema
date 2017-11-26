package ucles.weblab.common.xc.domain.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucles.weblab.common.xc.domain.CrossContextLink;

import java.net.URI;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA 2.1 converter to turn cross-context links into URNs for persistence.
 * To activate these converters, make sure your persistence provider detects it by including this class in the list of
 * mapped classes e.g. with the annotation property {@link org.springframework.boot.autoconfigure.domain.EntityScan#basePackages()}.
 *
 * @since 06/10/15
 */
public class CrossContextLinkJpaConverters {

    @Converter(autoApply = true)
    public static class CrossContextLinkConverter implements AttributeConverter<CrossContextLink, String> {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        public CrossContextLinkConverter() {
            logger.info("Registered CrossContextLink converters with JPA.");
        }

        @Override
        public String convertToDatabaseColumn(CrossContextLink attribute) {
            return attribute == null ? null : attribute.asUrn().toString();
        }

        @Override
        public CrossContextLink convertToEntityAttribute(String dbData) {
            return dbData == null ? null : new CrossContextLink(URI.create(dbData));
        }
    }
}
