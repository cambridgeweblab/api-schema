package ucles.weblab.common.xc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UriTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A cross context converter which introspects controllers to find URN&lt;-&gt;URL mappings and allows cross-context
 * links to be resolved using the controller method directly.
 *
 * At startup, it finds all Spring MVC handler methods in the context (via {@link RequestMappingHandlerMapping})
 * and looks for methods annotated with {@link CrossContextMapping @CrossContextMapping}.
 *
 * @since 06/10/15
 */
public class ControllerIntrospectingCrossContextConverter implements CrossContextConverter, ApplicationListener<ContextRefreshedEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private PathMatcher pathMatcher;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private ConversionService conversionService;

    private final Map<HandlerMethod, Pattern> urnPatterns = new HashMap<>();

    private final MultiValueMap<HandlerMethod, String> urlPatterns = new LinkedMultiValueMap<>();

    @Autowired
    void configurePathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Autowired
    void configureRequestMappingHandlerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Autowired
    @Qualifier("mvcConversionService")
    void configureConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                                  this.requestMappingHandlerMapping.getHandlerMethods();

        for(Map.Entry<RequestMappingInfo, HandlerMethod> item : handlerMethods.entrySet()) {
            RequestMappingInfo mapping = item.getKey();
            HandlerMethod method = item.getValue();

            CrossContextMapping xcMapping = method.getMethodAnnotation(CrossContextMapping.class);

            if (xcMapping != null) {
                String regex = xcMapping.value().replaceAll("\\{[A-za-z0-9_]+\\}", "([^/:]+)").replace("$", "\\$");
                urnPatterns.put(method, Pattern.compile(regex));

                for (String urlPattern : mapping.getPatternsCondition().getPatterns()) {
                    logger.info("Registered cross-context mapping between " + xcMapping.value() + " and " + urlPattern);
                    urlPatterns.add(method, urlPattern);
                }
            }
        }
    }

    @Override
    public URI toUrn(URI url) {
        for (Map.Entry<HandlerMethod, List<String>> methodPatterns : urlPatterns.entrySet()) {
            HandlerMethod handlerMethod = methodPatterns.getKey();
            CrossContextMapping xcMapping = handlerMethod.getMethodAnnotation(CrossContextMapping.class);
            for (String urlPattern : methodPatterns.getValue()) {
                if (pathMatcher.match(urlPattern, url.getPath())) {
                    Map<String, String> templateVariables = pathMatcher.extractUriTemplateVariables(urlPattern, url.getPath());
                    // Substitute the variables into the path
                    return new UriTemplate(xcMapping.value()).expand(templateVariables);
                }
            }
        }

        return null;
    }

    @Override
    public URI toUrl(URI urn) {
        return Optional.ofNullable(urnToHandlerMethodInvocation(urn))
                .map(m -> MvcUriComponentsBuilder.fromMethod(m.getHandlerMethod().getMethod(), m.getArgs())
                        .build().toUri())
                .orElse(null);
    }

    protected HandlerMethodInvocation urnToHandlerMethodInvocation(URI urn) {
        for (Map.Entry<HandlerMethod, Pattern> mapping : urnPatterns.entrySet()) {
            Matcher matcher = mapping.getValue().matcher(urn.toString());
            if (matcher.matches()) {
                // NB: linkTo() without controller ignores the parameters.
                HandlerMethod handlerMethod = mapping.getKey();
                CrossContextMapping xcMapping = handlerMethod.getMethodAnnotation(CrossContextMapping.class);
                Map<String, String> templateVariables = pathMatcher.extractUriTemplateVariables(xcMapping.value(), urn.toString());
                Object[] args = evaluateHandlerMethodArguments(handlerMethod, templateVariables);
                return new HandlerMethodInvocation(handlerMethod.createWithResolvedBean(), args);
            }
        }
        return null;
    }

    private Object[] evaluateHandlerMethodArguments(HandlerMethod method, Map<String, String> templateVariables) {
        MethodParameter[] parameters = method.getMethodParameters();
        final Object[] arguments = new Object[parameters.length];
        final Iterator<String> pathVariableValues = templateVariables.values().stream()
                .map(s -> {
                    try {
                        return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .iterator();

        // TODO: Support controller methods which take form posts with @RequestParam too
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            if (parameter.getParameterAnnotation(PathVariable.class) != null) {
                try {
                    if (parameter.getParameterType().isAssignableFrom(String.class)) {
                        arguments[i] = pathVariableValues.next();
                    } else {
                        arguments[i] = conversionService.convert(pathVariableValues.next(), parameter.getParameterType());
                    }
                } catch (NoSuchElementException e) {
                    logger.error("No path variable specified for parameter " + i + " [" + parameter + "]");
                    return null;
                }
            } else if (parameter.getParameterAnnotation(RequestBody.class) != null && ResourceSupport.class.isAssignableFrom(parameter.getParameterType())) {
                logger.debug("Skipping @RequestBody parameter " + i + " ");
            } else if (parameter.getParameterAnnotation(org.springframework.security.web.bind.annotation.AuthenticationPrincipal.class) != null //Keep for backward compatibility
                    || parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null
                    || Principal.class.isAssignableFrom(parameter.getParameterType())
                    || Authentication.class.isAssignableFrom(parameter.getParameterType())) {
                logger.debug("Skipping security parameter " + i + " [" + parameter + "]");
            } else if (parameter.getParameterAnnotation(RequestBody.class) == null || !ResourceSupport.class.isAssignableFrom(parameter.getParameterType())) {
                logger.error("Controller method " + method.toString() + " parameter " + i + " [" + parameter + "] is not a @PathVariable or a @RequestBody ResourceSupport");
                return null;
            }
        }
        return arguments;
    }


    /**
     * Describes the invocation of a handler method with particular parameters.
     */
    protected static class HandlerMethodInvocation {
        private final HandlerMethod handlerMethod;
        private final Object[] args;

        public HandlerMethodInvocation(HandlerMethod handlerMethod, Object... args) {
            this.handlerMethod = handlerMethod;
            this.args = args;
        }

        public HandlerMethod getHandlerMethod() {
            return handlerMethod;
        }

        public Object[] getArgs() {
            return args;
        }
    }

}
