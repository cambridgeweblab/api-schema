package ucles.weblab.common.xc.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import ucles.weblab.common.identity.domain.Belongs;
import ucles.weblab.common.xc.service.ControllerIntrospectingCrossContextConverter.HandlerMethodInvocation;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @since 06/10/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ControllerIntrospectingCrossContextConverterTest {

    @Mock
    ApplicationContext applicationContext;

    SettableRequestMappingHandlerMapping requestMappingHandlerMapping = new SettableRequestMappingHandlerMapping();

    ControllerIntrospectingCrossContextConverter converter = new ControllerIntrospectingCrossContextConverter();

    ConversionService conversionService = new DefaultFormattingConversionService();

    @Before
    public void setUpRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        converter.configurePathMatcher(new AntPathMatcher());
        converter.configureRequestMappingHandlerMapping(requestMappingHandlerMapping);
        converter.configureConversionService(conversionService);
    }


    @Test
    public void testConvertingFromUrn() {
        registerRequestHandler(TestController.class, "testMethod", String.class);

        converter.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

        URI url = converter.toUrl(URI.create("urn:xc:context:test:antelope"));
        assertEquals("Expect URL matching @RequestMapping", URI.create("http://localhost/api/test/antelope"), url);
    }

    @Test
    public void testConvertingFromUrnWithUuidArgument() {
        registerRequestHandler(TestController.class, "globallyUniqueMethod", UUID.class);

        converter.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

        URI url = converter.toUrl(URI.create("urn:xc:context:uuid:039918f4-0e79-40bb-97ce-fa39d0dcad93"));
        assertEquals("Expect URL matching @RequestMapping", URI.create("http://localhost/api/uuid/039918f4-0e79-40bb-97ce-fa39d0dcad93"), url);
    }

    @Test
    public void testConvertingFromUrl() {
        registerRequestHandler(TestController.class, "testMethod", String.class);

        converter.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

        URI urn = converter.toUrn(URI.create("http://localhost/api/test/horse"));
        assertEquals("Expect URN matching @CrossContextMapping", URI.create("urn:xc:context:test:horse"), urn);
    }

    @Test
    public void testConvertingFromUrnIgnoringAuthenticationPrincipal() {
        registerRequestHandler(TestController.class, "authenticatedMethod", Belongs.class, String.class);

        converter.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

        URI url = converter.toUrl(URI.create("urn:xc:context:authenticated:antelope"));
        assertEquals("Expect URL matching @RequestMapping", URI.create("http://localhost/api/authenticated/antelope"), url);
    }

    @Test
    public void testConvertingFromUrlIgnoringAuthenticationPrincipal() {
        registerRequestHandler(TestController.class, "authenticatedMethod", Belongs.class, String.class);

        converter.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

        URI urn = converter.toUrn(URI.create("http://localhost/api/authenticated/horse"));
        assertEquals("Expect URN matching @CrossContextMapping", URI.create("urn:xc:context:authenticated:horse"), urn);
    }

    @Test
    public void testFindingControllerMethod() {
        registerRequestHandler(TestController.class, "testMethod", String.class);

        converter.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

        URI urn = URI.create("urn:xc:context:test:koala");
        HandlerMethodInvocation methodInvocation = converter.urnToHandlerMethodInvocation(urn);
        assertEquals("Expect controller type", TestController.class, methodInvocation.getHandlerMethod().getBeanType());
        assertEquals("Expect controller method", "testMethod", methodInvocation.getHandlerMethod().getMethod().getName());
        assertArrayEquals("Expect controller argument", new Object[]{"koala"}, methodInvocation.getArgs());
    }

    private void registerRequestHandler(final Class<?> controllerClass, final String methodName, final Class<?>... paramTypes) {
        try {
            Method testMethod = ReflectionUtils.findMethod(controllerClass, methodName, paramTypes);
            RequestMappingInfo mappingInfo = requestMappingHandlerMapping.getMappingForMethod(testMethod, controllerClass);
            Object instance = controllerClass.newInstance();
            requestMappingHandlerMapping.registerHandlerMethod(instance, testMethod, mappingInfo);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Controller
    @RequestMapping("/api")
    static class TestController {
        @RequestMapping("/test/{id}")
        @CrossContextMapping("urn:xc:context:test:{id}")
        public String testMethod(@PathVariable String id) {
            return "who cares what this returns?";
        }

        @RequestMapping("/authenticated/{id}")
        @CrossContextMapping("urn:xc:context:authenticated:{id}")
        public String authenticatedMethod(@AuthenticationPrincipal Belongs principal, @PathVariable String id) {
            return "still don't care what it returns, though";
        }

        @RequestMapping("/uuid/{id}")
        @CrossContextMapping("urn:xc:context:uuid:{id}")
        public String globallyUniqueMethod(@PathVariable UUID id) {
            return "couldn't care less, honestly";
        }

    }

}
