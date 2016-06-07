/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ucles.weblab.common.xc.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 *
 * @author Sukhraj
 */
public class RestCrossContextConverter implements CrossContextConverter, ApplicationListener<ContextRefreshedEvent> {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, URI> urnToUrls = new HashMap<>();
    private final Map<URI, String> urlToUrns = new HashMap<>();

    //urn:xc:paymenttype:online:{centreNumber}:{id}
    public RestCrossContextConverter() { 
        urnToUrls.put("urn:xc:payment:purchaseorderno:{purchaseorderno}", URI.create("http://localhost:8080/api/payment/"));
        urlToUrns.put(URI.create("http://localhost:8080/api/payment"), "urn:xc:payment:purchaseorderno:{purchaseorderno}");    
    }
        
    @Override
    public URI toUrn(URI url) {
        String res = urlToUrns.get(url);
        return res != null? URI.create(res) : null;
    }

    @Override
    public URI toUrl(URI urn) {
        URI res = urnToUrls.get(urn.getPath());
        return res;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent e) {
        //do nothing for now
    }

    
}
