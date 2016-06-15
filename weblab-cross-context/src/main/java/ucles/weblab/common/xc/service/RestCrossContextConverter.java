package ucles.weblab.common.xc.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 *
 * @author Sukhraj
 */
public class RestCrossContextConverter implements CrossContextConverter, ApplicationListener<ContextRefreshedEvent> {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, URI> urnToUrls = new HashMap<>();
    private final Map<URI, String> urlToUrns = new HashMap<>();

    private final RestSettings restSettings;    
    
    public RestCrossContextConverter(RestSettings restSettings) { 
    
        this.restSettings = restSettings;
        List<String> urns = restSettings.getUrns();
        List<String> urls = restSettings.getUrls();
        
        if (urns != null && !urns.isEmpty()) {
            //go through the urns and fill the maps
            IntStream.range(0, urns.size()).forEach(index -> {
                String url = urls.get(index);
                String urn = urns.get(index);
                urnToUrls.put(urn, URI.create(url));
                urlToUrns.put(URI.create(url), urn);
            });
        }
    }
        
    @Override
    public URI toUrn(URI url) {
        String res = urlToUrns.get(url);
        return res != null? URI.create(res) : null;
    }

    @Override
    public URI toUrl(URI urn) {
        URI res = urnToUrls.get(urn.toString());
        return res;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent e) {
        //do nothing for now
    }

}
