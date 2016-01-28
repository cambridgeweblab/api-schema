package ucles.weblab.common.xc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import ucles.weblab.common.xc.service.ControllerIntrospectingCrossContextConverter.HandlerMethodInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;

import static org.mockito.Mockito.when;

/**
 * @since 09/01/16
 */
public class HandlerMethodInvokingCrossContextResolverTest {
    SettableRequestMappingHandlerMapping requestMappingHandlerMapping = new SettableRequestMappingHandlerMapping();
    ControllerIntrospectingCrossContextConverter converter = Mockito.mock(ControllerIntrospectingCrossContextConverter.class);
    ObjectMapper objectMapper = new ObjectMapper();
    HandlerMethodInvokingCrossContextResolver resolver = new HandlerMethodInvokingCrossContextResolver(converter, objectMapper);

    @Test
    public void testUrnToJson() {
        URI urn = URI.create("urn:xc:context:test:supercasino");
        HandlerMethod handlerMethod = createHandlerMethod(TestController.class, "testMethod", String.class);
        when(converter.urnToHandlerMethodInvocation(Matchers.eq(urn)))
                .thenReturn(new HandlerMethodInvocation(handlerMethod, "supercasino"));

        JsonNode jsonNode = resolver.urnToJson(urn);
        Assert.assertTrue("Expect JSON object", jsonNode.isObject());
        Assert.assertEquals("Expect single property", "supercasino", jsonNode.path("name").asText());
    }

    @Test
    public void testUrnToImplementedInterface() {
        URI urn = URI.create("urn:xc:context:test:supercasino");
        HandlerMethod handlerMethod = createHandlerMethod(TestController.class, "testMethod", String.class);
        when(converter.urnToHandlerMethodInvocation(Matchers.eq(urn)))
                .thenReturn(new HandlerMethodInvocation(handlerMethod, "supercasino"));

        NamedResource hasName = resolver.urnToValue(urn, NamedResource.class);
        Assert.assertNotNull("Expect object", hasName);
        Assert.assertEquals("Expect original object", TestResource.class, hasName.getClass());
    }

    // This interface matches the returned object via duck typing
    interface HasName {
        String getName();
    }

    @Test
    public void testUrnToProxiedInterface() {
        URI urn = URI.create("urn:xc:context:test:supercasino");
        HandlerMethod handlerMethod = createHandlerMethod(TestController.class, "testMethod", String.class);
        when(converter.urnToHandlerMethodInvocation(Matchers.eq(urn)))
                .thenReturn(new HandlerMethodInvocation(handlerMethod, "supercasino"));

        HasName hasName = resolver.urnToValue(urn, HasName.class);
        Assert.assertNotNull("Expect object", hasName);
        Assert.assertTrue("Expect proxy object", Proxy.isProxyClass(hasName.getClass()));
    }

    // This class matches but we don't do cglib proxying, so we need to convert via Jackson
    static class AlternativeResource {
        private String name;

        public String getName() {
            return name;
        }
    }

    @Test
    public void testUrnToAlternativeType() {
        URI urn = URI.create("urn:xc:context:wrapped:pokerface");
        HandlerMethod handlerMethod = createHandlerMethod(TestController.class, "wrappedMethod", String.class);
        when(converter.urnToHandlerMethodInvocation(Matchers.eq(urn)))
                .thenReturn(new HandlerMethodInvocation(handlerMethod, "pokerface"));

        AlternativeResource alternativeResource = resolver.urnToValue(urn, AlternativeResource.class);
        Assert.assertNotNull("Expect object", alternativeResource);
        Assert.assertEquals("Expect correct name", "pokerface", alternativeResource.getName());
    }

    private HandlerMethod createHandlerMethod(final Class<?> controllerClass, final String methodName, final Class<?>... paramTypes) {
        try {
            Method testMethod = ReflectionUtils.findMethod(controllerClass, methodName, paramTypes);
            Object instance = controllerClass.newInstance();
            return requestMappingHandlerMapping.createHandlerMethod(instance, testMethod);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    interface NamedResource {
        String getName();
    }

    static class TestResource implements NamedResource {
        String name;

        protected TestResource() {}

        public TestResource(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Controller
    @RequestMapping("/api")
    static class TestController {
        @RequestMapping("/test/{id}")
        @CrossContextMapping("urn:xc:context:test:{id}")
        public TestResource testMethod(@PathVariable String id) {
            return new TestResource(id);
        }

        @RequestMapping("/wrapped/{id}")
        @CrossContextMapping("urn:xc:context:wrapped:{id}")
        public ResponseEntity<TestResource> wrappedMethod(@PathVariable String id) {
            return ResponseEntity.ok(new TestResource(id));
        }
    }


}
