package ucles.weblab.common.xc.service;

import com.fasterxml.jackson.databind.JsonNode;
import ucles.weblab.common.xc.domain.CrossContextLink;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service delegating to list of resolvers to resolve cross context links to their referenced data.
 * {@link HandlerMethodInvokingCrossContextResolver} is added as a default resolver, and in the case where all the
 * bounded contexts are hosted in the same container, is all you need.
 *
 * @since 09/01/16
 */
public class CrossContextResolverServiceImpl implements CrossContextResolverService {
    final List<CrossContextResolver> resolvers = new ArrayList<>();

    @Override
    public void addResolver(CrossContextResolver resolver) {
        this.resolvers.add(resolver);
    }

    public void setResolvers(List<CrossContextResolver> resolvers) {
        this.resolvers.clear();
        this.resolvers.addAll(resolvers);
    }

    @Override
    public JsonNode resolveAsJson(CrossContextLink xcl) {
        URI urn = xcl.asUrn();

        return resolvers.stream()
                .map(xcr -> xcr.urnToJson(urn))
                .filter(node -> node != null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find a resolver for the link: " + xcl));

    }

    @Override
    public <T> T resolveAsValue(CrossContextLink xcl, Class<T> type) {
        URI urn = xcl.asUrn();

        return resolvers.stream()
                .map(xcr -> xcr.urnToValue(urn, type))
                .filter(value -> value != null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find a resolver for the link: " + xcl));
    }
}
