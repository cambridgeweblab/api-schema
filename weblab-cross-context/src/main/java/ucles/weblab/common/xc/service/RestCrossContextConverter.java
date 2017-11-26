package ucles.weblab.common.xc.service;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
/**
 *
 * @author Sukhraj
 */
public class RestCrossContextConverter implements CrossContextConverter, ApplicationListener<ContextRefreshedEvent> {

    private final Map<String, URI> urnToUrls = new HashMap<>();
    private final Map<URI, String> urlToUrns = new HashMap<>();

    public RestCrossContextConverter(RestSettings restSettings) {

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
        return res == null ? null : URI.create(res);
    }

    @Override
    public URI toUrl(URI urn) {
        return urnToUrls.get(urn.toString());
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent e) {
        //do nothing for now
    }

}
