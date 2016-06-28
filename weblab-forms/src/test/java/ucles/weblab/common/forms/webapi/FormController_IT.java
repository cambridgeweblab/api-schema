package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import java.io.InputStream;
import java.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.FormRepository;
import ucles.weblab.common.forms.domain.mongo.FormFactoryMongo;
import ucles.weblab.common.forms.domain.mongo.FormRepositoryMongo;
import ucles.weblab.common.schema.webapi.ControllerMethodSchemaCreator;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.test.webapi.AbstractRestController_IT;
import ucles.weblab.common.xc.service.CrossContextConversionService;
import ucles.weblab.common.xc.service.CrossContextConversionServiceImpl;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ucles.weblab.common.schema.webapi.SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE;
/**
 *
 * @author Sukhraj
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@WebIntegrationTest(value = "classpath:/public", randomPort = true)
//@Transactional
public class FormController_IT extends AbstractRestController_IT {
 
    private static final Logger log = LoggerFactory.getLogger(FormController_IT.class);
    
    @Autowired
    private MongoTemplate mongoTemplate; 
    
    @Autowired
    private FormDelegate formDelegate;    
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Configuration
    @EnableMongoRepositories(basePackageClasses = {FormRepositoryMongo.class})
    @Import({SecurityAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class})
    @ComponentScan(basePackageClasses = {FormController.class})
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
            CrossContextConversionServiceImpl crossContextConversionService = new CrossContextConversionServiceImpl();
            return crossContextConversionService;
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
                                                           JsonSchemaFactory jsonSchemaFactory) {

            return new ResourceSchemaCreator(securityChecker, 
                                            new ObjectMapper(), 
                                            crossContextConversionService, 
                                            enumSchemaCreator, 
                                            jsonSchemaFactory);
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
                                                                    EnumSchemaCreator enumSchemaCreator) {
            return new ControllerMethodSchemaCreator(objectMapper, crossContextConversionService, enumSchemaCreator);
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
        mongoTemplate.remove(new Query(), "forms");
    }
    
    @After
    public void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
        mongoTemplate.remove(new Query(), "forms");
    }
    
    @Test
    public void testSave() throws Exception {
       
        ObjectMapper mapper = new ObjectMapper();
        final InputStream resource = getClass().getResourceAsStream("test-schema.json");
        JsonNode node = mapper.readTree(resource);
       
        FormResource form = new FormResource(null, 
                                            "my-test-form-name",
                                            "my-test-form-description",
                                            "test-webapp", 
                                            "ca-business-stream", 
                                            node,
                                            Instant.now(),
                                            Instant.now());
        
        String jsonString = json(form);
        log.debug("JSON data to POST: " + jsonString);
                
        ResultActions postResult = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(jsonString))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form-name")));
    }       
    
    @Test
    public void testGetSchema() throws Exception {
                       
        ResultActions result = mockMvc.perform(get("/api/forms/$schema/")
                .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_SCHEMA_JSON_UTF8_VALUE));
        
        log.debug("Schema is: " + result.andReturn().getResponse().getContentAsString());
    }
}