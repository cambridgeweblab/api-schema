package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
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
import ucles.weblab.common.forms.domain.FormRepository;
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
            return new FormResourceAssembler();
        }
        
        @Bean
        FormDelegate formDelegate(FormRepository formRepository,
                                    FormResourceAssembler formAssembler) {
        
            return new FormDelegate(formRepository, formAssembler);
            
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
        SchemaFactoryWrapper wrapper = new SchemaFactoryWrapper();
        objectMapper.acceptJsonFormatVisitor(objectMapper.constructType(TestBean.class), wrapper);
        JsonSchema finalSchema = wrapper.finalSchema();
                
        FormResource form = new FormResource("my-new-form", "test-webapp", "ca-business-stream", finalSchema);
        
        String jsonString = json(form);
        
        ResultActions postResult = mockMvc.perform(post("/api/form/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(jsonString))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name", is("my-new-form")));
    }
    
    static class TestBean{
       private String property1;
       private double property2;
       private String[] property3;
       
       protected TestBean() {
           
       }
       
       public TestBean(String prop1, double prop2, String[] prop3) {
           this.property1 = prop1;
           this.property2 = prop2;
           this.property3 = prop3;
                   
       }

        public String getProperty1() {
            return property1;
        }

        public double getProperty2() {
            return property2;
        }

        public String[] getProperty3() {
            return property3;
        }       
    }
}
