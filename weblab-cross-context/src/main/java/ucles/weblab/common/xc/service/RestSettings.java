package ucles.weblab.common.xc.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sukhraj
 */
@Configuration
@ConfigurationProperties(prefix = "weblab.crosscontext")
public class RestSettings {
         
    private List<String> urns = new ArrayList<>();
    
    private List<String> urls = new ArrayList<>();

    public List<String> getUrns() {
        return urns;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrns(List<String> urns) {
        this.urns = urns;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
    
    
}
