package ucles.weblab.common.xc.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be applied to controller methods to define the mapping to a persistent URN.
 * Analogous to {@link org.springframework.web.bind.annotation.RequestMapping @RequestMapping}.
 *
 * @since 06/10/15
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrossContextMapping {
    /**
   	 * Defines a mapping from the annotated method's URL to and from an opaque URI for persistence.
     * The mapping must be a URN (i.e. it must be a string representation of a URI with the {@code urn} scheme).
     * The namespace for the URN is {@code xc} (as a shorthand for cross-context), so the URI must start with
     * {@code urn:xc:} and be followed by further components to unique identify the resource in a persistent,
     * location-independent manner (e.g. {@code urn:xc:orders:salesOrder:3214}. It is suggested that the URN
     * incorporate both the context ("orders" in this example) and the type ("salesOrder"),
     * as well as an identifier which uniquely identifies an instance of that type.
     * <p>
     * {@link ControllerIntrospectingCrossContextConverter} will use these annotations to convert between
     * the URN configured by this annotation and the URL configured by {@code @RequestMapping}. Therefore all
     * placeholders should be consistent between the {@code @RequestMapping} and the {@code CrossContextMapping}.
   	 */
    String value();
}
