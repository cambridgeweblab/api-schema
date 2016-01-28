package ucles.weblab.common.xc.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;

/**
 * Interface for a class which can resolve cross-context URNs to their referenced data (in order to use data from one
 * context in another, on the rare occasions that is necessary).
 * The resolvers are chained, so the conversion methods return null if this resolver cannot
 * help.
 *
 * @since 09/01/16
 */
public interface CrossContextResolver {
    JsonNode urnToJson(URI urn);
    <T> T urnToValue(URI urn, Class<T> type);
}
