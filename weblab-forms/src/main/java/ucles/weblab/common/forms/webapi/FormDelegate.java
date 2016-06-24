package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormEntity;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.FormRepository;
import ucles.weblab.common.forms.domain.ImmutableForm;
import ucles.weblab.common.forms.exception.BadDataException;

/**
 * A delegate class sitting between controllers and repositories.
 * 
 * @author Sukhraj
 */
public class FormDelegate {
   
    private static final Logger log = LoggerFactory.getLogger(FormDelegate.class);
    
    private final FormRepository formRepository;
    private final FormResourceAssembler formAssembler; 
    private final FormFactory formFactory;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormDelegate(FormRepository formRepository,
                        FormResourceAssembler formAssembler,
                        FormFactory formFactory,
                        ObjectMapper objectMapper) {
        this.formRepository = formRepository;
        this.formAssembler = formAssembler;    
        this.formFactory = formFactory;
        this.objectMapper = objectMapper;
    }
    
    public FormResource create(FormResource resource) {
        String stringValue = null;
        try {
            stringValue = objectMapper.writeValueAsString(resource.getSchema());
        } catch (JsonProcessingException ex) {
            log.error("Can not convert schema to a string", ex);
            throw new BadDataException("Can not convert schema to a string", null);
        }
        
        Form vo = ImmutableForm.builder()
                                .applicationName(resource.getApplicationName())
                                .businessStream(resource.getBusinessStream())
                                .name(resource.getName())
                                .schema(stringValue)
                                .build(); 

        FormEntity formEntity = formFactory.newFormEntity(vo);
        
        FormEntity saved = formRepository.save(formEntity);
        
        FormResource returned = toResource(saved);
        
        return returned;
        
    }
    
    private FormResource toResource(FormEntity entity) {
        return formAssembler.toResource(entity);
    }
   
}