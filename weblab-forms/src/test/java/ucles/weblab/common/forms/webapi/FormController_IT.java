package ucles.weblab.common.forms.webapi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ucles.weblab.common.test.webapi.AbstractRestController_IT;
import static org.junit.Assert.assertTrue;
/**
 *
 * @author Sukhraj
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@WebIntegrationTest(value = "classpath:/public", randomPort = true)
public class FormController_IT extends AbstractRestController_IT {
 
    
    @Test
    public void testSomething() throws Exception {
        assertTrue(true);
    }
}
