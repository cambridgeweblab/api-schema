package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.FormRepository;
import ucles.weblab.common.forms.domain.mongo.FormFactoryMongo;
import ucles.weblab.common.forms.domain.mongo.FormRepositoryMongo;
import ucles.weblab.common.test.webapi.AbstractRestController_IT;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 *
 * @author Sukhraj
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@WebIntegrationTest(value = "classpath:/public", randomPort = true)
//@Transactional
public class FormController_IT extends AbstractRestController_IT {
 
    @Autowired
    private MongoTemplate mongoTemplate; 
    
    @Autowired
    private FormDelegate formDelegate;    
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Configuration
    @EnableMongoRepositories(basePackageClasses = {FormRepositoryMongo.class})
    @Import({MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class})
    @ComponentScan(basePackageClasses = {FormController.class})
    @EnableAutoConfiguration
    public static class Config {
        
        
        
        @Bean
        FormResourceAssembler formResourceAssembler() {
            return new FormResourceAssembler(new ObjectMapper());
        }
        
        @Bean
        FormFactory formFactory() {
            return new FormFactoryMongo();
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
    @Ignore
    public void testSave() throws Exception {
       
        ObjectMapper mapper = new ObjectMapper();
        final InputStream resource = getClass().getResourceAsStream("test-schema.json");
        JsonNode node = mapper.readTree(resource);
        
        FormResource form = new FormResource("my-test-form", "test-webapp", "ca-business-stream", node);
        
        String jsonString = json(form);
        System.out.println("JSON data to POST: " + jsonString);
                
        ResultActions postResult = mockMvc.perform(post("/api/forms/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(jsonString))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-test-form")));
    }                 
}