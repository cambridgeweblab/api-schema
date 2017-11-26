package ucles.weblab.common.xc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import ucles.weblab.common.xc.service.ControllerIntrospectingCrossContextConverter.HandlerMethodInvocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;

/**
 * A cross context resolver which invokes controller methods directly to resolve links into data.
 *
 * It relies on the {@link ControllerIntrospectingCrossContextConverter} to find the appropriate handler methods.
 * <p>
 * This class's implementation of {@link #urnToValue(URI, Class)} shortcuts the translation via wire format in the event
 * that the data returned by the direct invocation of the controller method either implements the requested interface
 * (in which case the controller return value is used directly) or is duck-typing compatible with it (in which case a
 * dynamic proxy is used to delegate to the controller return value). In any other case, Jackson is used to translate
 * from one object to the other using {@link ObjectMapper#convertValue(Object, Class)}.
 *
 * @since 09/01/16
 */
public class HandlerMethodInvokingCrossContextResolver implements CrossContextResolver {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ControllerIntrospectingCrossContextConverter converter;
    private final ObjectMapper objectMapper;

    public HandlerMethodInvokingCrossContextResolver(ControllerIntrospectingCrossContextConverter converter, ObjectMapper objectMapper) {
        this.converter = converter;
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode urnToJson(URI urn) {
        HandlerMethodInvocation methodInvocation = converter.urnToHandlerMethodInvocation(urn);
        if (methodInvocation != null) {
            Object result = invokeHandler(methodInvocation);
            if (result != null) {
                return objectMapper.convertValue(result, JsonNode.class);
            }
        }
        return null;
    }

    private Object invokeHandler(HandlerMethodInvocation methodInvocation) {
        try {
            Object result = methodInvocation.getHandlerMethod().getMethod().invoke(
                    methodInvocation.getHandlerMethod().getBean(),
                    methodInvocation.getArgs()
            );
            if (result instanceof ResponseEntity) {
                if (((ResponseEntity) result).getStatusCode().is2xxSuccessful()) {
                    // unwrap
                    result = ((ResponseEntity) result).getBody();
                } else {
                    logger.error("Method " + methodInvocation.getHandlerMethod().getMethod() + " returned an HTTP error " + ((ResponseEntity) result).getStatusCode());
                    result = null;
                }
            }
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Unable to invoke handler method " + methodInvocation.getHandlerMethod().getMethod().toString());
            return null;
        }
    }

    @Override
    public <T> T urnToValue(URI urn, Class<T> type) {
        HandlerMethodInvocation methodInvocation = converter.urnToHandlerMethodInvocation(urn);
        if (methodInvocation != null) {
            Object result = invokeHandler(methodInvocation);
            if (result != null) {
                Class<?> resultType = result.getClass();
                // if resultType can be assigned to type, we can return it directly
                if (type.isAssignableFrom(resultType)) {
                    return (T) result;
                }
                // if type is an interface, then take a shortcut - if the returned value implements all the methods
                // then we can generate a Proxy (duck typing) instead of using Jackson to convert the value.
                if (type.isInterface() && isDuckTyped(type, resultType)) {
                    logger.debug(resultType + " is duck typed to interface " + type + " - returning dynamic proxy.");
                    return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{type},
                            (proxy, method, args) -> {
                                Method target = ReflectionUtils.findMethod(resultType, method.getName(), method.getParameterTypes());
                                return target.invoke(result, args);
                            });
                } else {
                    return objectMapper.convertValue(result, type);
                }
            }
        }
        return null;
    }

    private <T> boolean isDuckTyped(Class<T> pattern, Class<?> typeToCheck) {
        boolean duckTyped = true;
        for (Method method : pattern.getMethods()) {
            if (ReflectionUtils.findMethod(typeToCheck, method.getName(), method.getParameterTypes()) == null) {
                duckTyped = false;
                break;
            }
        }
        return duckTyped;
    }

}
