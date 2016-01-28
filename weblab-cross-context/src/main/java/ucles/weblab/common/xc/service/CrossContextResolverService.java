package ucles.weblab.common.xc.service;

import com.fasterxml.jackson.databind.JsonNode;
import ucles.weblab.common.xc.domain.CrossContextLink;

/**
 * Interface for service to resolve cross-context links to the underlying data.
 * Allows one bounded context to obtain data from another. Care should be taken as this may involve a remote call.
 *
 * @since 08/01/16
 */
public interface CrossContextResolverService {
    void addResolver(CrossContextResolver resolver);

    /**
     * Fetch the data from a cross-context link as JSON.
     * <p>
     * This method is most efficient if the cross-context link is internally a URN (i.e. not obtained from an API request)
     * and, particularly, if it can be resolved by a local rather than remote call.
     *
     * @param xcl the cross-context link
     * @return the result of requesting data from the cross-context link, as a JSON tree
     */
    JsonNode resolveAsJson(CrossContextLink xcl);

    /**
     * Fetch the data from a cross-context link as data of an arbitrary type.
     * The type can be any class or interface which is wire-compatible with the cross-context link.
     * If it's an interface, then the returned value will typically be a proxy.
     * <p>
     * This method is most efficient if the cross-context link is internally a URN (i.e. not obtained from an API request)
     * and, particularly, if it can be resolved by a local rather than remote call and an interface type is given such that
     * the original data can be proxied.
     *
     * @param xcl the cross-context link
     * @param type the type desired (interface or class)
     * @return the result of requesting data from the cross-context link, converted to the desired type
     */
    <T> T resolveAsValue(CrossContextLink xcl, Class<T> type);
}
