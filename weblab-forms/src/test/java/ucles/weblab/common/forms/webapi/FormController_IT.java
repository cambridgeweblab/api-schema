package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.NestedServletException;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.FormRepository;
import ucles.weblab.common.forms.domain.mongo.FormFactoryMongo;
import ucles.weblab.common.forms.domain.mongo.FormRepositoryMongo;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.i18n.service.impl.LocalisationServiceImpl;
import ucles.weblab.common.schema.webapi.ControllerMethodSchemaCreator;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.test.webapi.AbstractRestController_IT;
import ucles.weblab.common.xc.service.CrossContextConversionService;
import ucles.weblab.common.xc.service.CrossContextConversionServiceImpl;
import ucles.weblab.common.xc.service.CrossContextConverter;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ucles.weblab.common.schema.webapi.SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE;
/**
 *
 * @author Sukhraj
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(value = "classpath:/public", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Transactional
public class FormController_IT extends AbstractRestController_IT {

    private static final Logger log = LoggerFactory.getLogger(FormController_IT.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CrossContextConversionServiceImpl conversionService;

    /*A Mock to map test urls to test urns */
    private final CrossContextConverter convertor = Mockito.mock(CrossContextConverter.class);
    private final String xcBusinessStreamUrl = "http://localhost:8080/api/forms/businessstreams/";
    private final String xcBusinessStreamUrn = "urn:xc:form:businessstreams";

    @Configuration
    @EnableMongoRepositories(basePackageClasses = {FormRepositoryMongo.class})
    @Import({SecurityAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class})
    @ComponentScan(basePackageClasses = {FormController.class, FormDelegate.class})
    @EnableAutoConfiguration
    public static class Config {

        @Bean
        @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
        MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
            return new DefaultMethodSecurityExpressionHandler();
        }

        @Bean
        SecurityChecker securityChecker(MethodSecurityExpressionHandler handler) {
            return new SecurityChecker(handler);
        }

        @Bean
        CrossContextConversionService crossContextConversionService() {
            return new CrossContextConversionServiceImpl();
        }

        @Bean
        LocalisationService localisationService(MessageSource messageSource) {
            return new LocalisationServiceImpl(messageSource);
        }

        @Bean
        EnumSchemaCreator enumSchemaCreator(final JsonSchemaFactory schemaFactory) {
            return new EnumSchemaCreator();
        }

        @Bean
        JsonSchemaFactory jsonSchemaFactory() {
            return new JsonSchemaFactory();
        }

        @Bean
        public ResourceSchemaCreator resourceSchemaCreator(SecurityChecker securityChecker,
                                                           CrossContextConversionService crossContextConversionService,
                                                           EnumSchemaCreator enumSchemaCreator,
                                                           JsonSchemaFactory jsonSchemaFactory,
                                                           LocalisationService localisationService) {

            return new ResourceSchemaCreator(securityChecker,
                                            new ObjectMapper(),
                                            crossContextConversionService,
                                            enumSchemaCreator,
                                            jsonSchemaFactory,
                                            localisationService);
        }

        @Bean
        FormResourceAssembler formResourceAssembler() {
            return new FormResourceAssembler(new ObjectMapper());
        }

        @Bean
        FormFactory formFactory() {
            return new FormFactoryMongo();
        }

        @Bean
        ControllerMethodSchemaCreator controllerMethodSchemaCreator(ObjectMapper objectMapper,
                                                                    CrossContextConversionService crossContextConversionService,
                                                                    EnumSchemaCreator enumSchemaCreator,
                                                                    LocalisationService localisationService) {
            return new ControllerMethodSchemaCreator(objectMapper, crossContextConversionService, enumSchemaCreator,
                    localisationService);
        }

        @Bean
        FormDelegate formDelegate(FormRepository formRepository,
                                  FormResourceAssembler formAssembler,
                                  FormFactory formFactory) {

            return new FormDelegate(formRepository, formAssembler, formFactory, new ObjectMapper());

        }


    }

    @Before
    public void runBefore() throws Exception {
        Mockito.when(convertor.toUrl(URI.create(xcBusinessStreamUrn))).thenReturn(URI.create(xcBusinessStreamUrl));
        Mockito.when(convertor.toUrn(URI.create(xcBusinessStreamUrl))).thenReturn(URI.create(xcBusinessStreamUrn));
        conversionService.addConverter(convertor);
        mongoTemplate.remove(new Query(), "forms");
        mongoTemplate.remove(new Query(), "formEntity");
    }

    @After
    public void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
        mongoTemplate.remove(new Query(), "forms");
        mongoTemplate.remove(new Query(), "formEntity");
    }

    @Test
    public void testSaveAndView() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        final InputStream resource = getClass().getResourceAsStream("test-schema.json");
        JsonNode node = mapper.readTree(resource);

        FormResource form = new FormResource("my-form-id",
                                            "my-test-form-name",
                                            "my-test-form-description",
                                            "test-webapp",
                                            Arrays.asList("ca-business-stream"),
                                            node,
                                            Instant.now(),
                                            Instant.now());

        String jsonString = json(form);
        log.debug("JSON data to POST: " + jsonString);

        final CompletableFuture<String> location = new CompletableFuture<>();

        ResultActions postResult = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(jsonString))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-name")))
                .andDo(r -> location.complete(r.getResponse().getHeader(HttpHeaders.LOCATION)));

        log.info("Post result: " + postResult.andReturn().getResponse().getContentAsString());


    }

    @Test
    public void testGetSchema() throws Exception {

        ResultActions result = mockMvc.perform(get("/api/forms/$schema/")
                .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_SCHEMA_JSON_UTF8_VALUE));

        log.debug("Schema is: " + result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testSaveAndList() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final InputStream resource = getClass().getResourceAsStream("test-schema.json");
        JsonNode node = mapper.readTree(resource);

        //post 2 forms for the same business stream and same application name
        FormResource form = new FormResource("my-form-id-1",
                                            "my-test-form-name-1",
                                            "my-test-form-description-1",
                                            "test-webapp",
                                            Arrays.asList("ca-business-stream"),
                                            node,
                                            Instant.now(),
                                            Instant.now());

        final CompletableFuture<String> location = new CompletableFuture<>();

        ResultActions postResult = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json(form)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-name-1")))
                .andDo(r -> location.complete(r.getResponse().getHeader(HttpHeaders.LOCATION)));


        form = new FormResource("my-form-id-2",
                                "my-test-form-name-2",
                                "my-test-form-description-2",
                                "test-webapp",
                                Arrays.asList("ca-business-stream"),
                                node,
                                Instant.now(),
                                Instant.now());

        ResultActions postResult2 = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json(form)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-name-2")))
                .andDo(r -> location.complete(r.getResponse().getHeader(HttpHeaders.LOCATION)));

        //post a form for a different application and a different business stream
        form = new FormResource("my-form-id-3",
                                "my-test-form-name-3",
                                "my-test-form-description-3",
                                "some-different-webapp",
                                Arrays.asList("ca-different-business-stream"),
                                node,
                                Instant.now(),
                                Instant.now());

        ResultActions postResult3 = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json(form)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-name-3")))
                .andDo(r -> location.complete(r.getResponse().getHeader(HttpHeaders.LOCATION)));

        //check that they return 2
        MvcResult return1 = mockMvc.perform(get("/api/forms/?businessStream=ca-business-stream&applicationName=test-webapp")
                .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.list", hasSize(2)))
                .andExpect(jsonPath("$.list[0].name", is("my-test-form-name-1")))
                .andExpect(jsonPath("$.list[1].name", is("my-test-form-name-2")))
                .andReturn();
    }

    /**
     * Ignoring this test until the PUT (update) of a form is working.
     *
     * @throws Exception
     */
    @Test
    public void testSaveThenUpdate() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        final InputStream resource = getClass().getResourceAsStream("test-schema.json");
        JsonNode node = mapper.readTree(resource);

        //post a form for a different application and a different business stream
        FormResource form = new FormResource("my-form-id-forsaveandupdate",
                                "my-test-form-name-3",
                                "my-test-form-description-3",
                                "some-different-webapp",
                                Arrays.asList("ca-different-business-stream"),
                                node,
                                Instant.now(),
                                Instant.now());

        ResultActions postResult = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json(form)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-name-3")));

        //create an updated resource
        form = new FormResource("my-form-id-forsaveandupdate",
                                "my-test-form-updated-name",
                                "my-test-form-updated-description",
                                "some-different-webapp",
                                Arrays.asList("ca-different-business-stream"),
                                node,
                                Instant.now(),
                                Instant.now());

        ResultActions postResultUpdate = mockMvc.perform(put("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json(form)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-updated-name")));

    }

    @Test(expected = NestedServletException.class)
    public void testSaveThenDelete() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final InputStream resource = getClass().getResourceAsStream("test-schema.json");
        JsonNode node = mapper.readTree(resource);

        //post a form for a different application and a different business stream
        FormResource form = new FormResource("my-form-id-forsaveanddelete",
                                "my-test-form-name-4",
                                "my-test-form-description-4",
                                "some-different-webapp",
                                Arrays.asList("ca-different-business-stream"),
                                node,
                                Instant.now(),
                                Instant.now());
        final CompletableFuture<String> location = new CompletableFuture<>();

        ResultActions postResult = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json(form)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-name-4")))
                .andDo(r -> location.complete(r.getResponse().getHeader(HttpHeaders.LOCATION)));

        mockMvc.perform(get(location.get()))
                .andExpect(status().isOk());

        mockMvc.perform(delete(location.get()))
                .andExpect(status().isOk());

        mockMvc.perform(get(location.get())); // Should throw exception.
    }
}