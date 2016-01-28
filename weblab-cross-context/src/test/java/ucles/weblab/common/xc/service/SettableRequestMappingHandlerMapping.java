package ucles.weblab.common.xc.service;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * Makes a couple of methods public.
 *
 * @since 09/01/16
 */
class SettableRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    public void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        super.registerHandlerMethod(handler, method, mapping);
    }

    @Override
    public RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        return super.getMappingForMethod(method, handlerType);
    }

    @Override
    public HandlerMethod createHandlerMethod(Object handler, Method method) {
        return super.createHandlerMethod(handler, method);
    }
}
