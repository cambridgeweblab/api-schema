package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormEntity;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.FormRepository;
import ucles.weblab.common.forms.domain.ImmutableForm;
import ucles.weblab.common.forms.exception.BadDataException;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;

import static java.util.stream.Collectors.toList;

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
            stringValue = objectMapper.writeValueAsString(resource.getFormDefinition());
        } catch (JsonProcessingException ex) {
            log.error("Can not convert schema to a string", ex);
            throw new BadDataException("Can not convert schema to a string", null);
        }
        
        Form vo = ImmutableForm.builder()
                                .id(resource.getFormId())
                                .applicationName(resource.getApplicationName())
                                .businessStreams(resource.getBusinessStreams())
                                .name(resource.getName())
                                .schema(stringValue)
                                .description(resource.getDescription())
                                .validFrom(resource.getValidFrom())
                                .validTo(resource.getValidTo())
                                .build(); 

        FormEntity formEntity = formFactory.newFormEntity(vo);
        
        FormEntity saved = formRepository.save(formEntity);
        
        FormResource returned = toResource(saved);
        
        return returned;
        
    }
    
    public FormResource get(String id) {
        
        FormEntity formEntity = formRepository.findOne(id);
        if (formEntity == null) {
            throw new ResourceNotFoundException(id);
        }
        FormResource resource = toResource(formEntity);
        
        return resource;
    }
    
    public List<FormResource> list(String businessStream, String applicationName) {
        List<? extends FormEntity> entities = formRepository.findByBusinessStreamsContainingAndApplicationName(businessStream, applicationName);
        List<FormResource> result = entities.stream().map(formAssembler::toResource).collect(toList());

        return result;
    }
    
    public FormResource update(FormResource resource) {
        FormEntity exisitingFormEntity = formRepository.findOne(resource.getFormId());
        if (exisitingFormEntity == null) {
            throw new ResourceNotFoundException(resource.getFormId());
        }
        String stringValue = null;
        try {
            stringValue = objectMapper.writeValueAsString(resource.getFormDefinition());
        } catch (JsonProcessingException ex) {
            log.error("Can not convert schema to a string", ex);
            throw new BadDataException("Can not convert schema to a string", null);
        }

        exisitingFormEntity.setDescription(resource.getDescription());
        exisitingFormEntity.setName(resource.getName());
        exisitingFormEntity.setApplicationName(resource.getApplicationName());
        exisitingFormEntity.setBusinessStreams(resource.getBusinessStreams());
        exisitingFormEntity.setDescription(resource.getDescription());
        exisitingFormEntity.setSchema(stringValue);
        exisitingFormEntity.setValidFrom(resource.getValidFrom());
        exisitingFormEntity.setValidTo(resource.getValidTo());
        
        FormEntity saved = formRepository.save(exisitingFormEntity);
        FormResource savedResource = toResource(saved);
        
        return savedResource;
    }
    
    private FormResource toResource(FormEntity entity) {
        return formAssembler.toResource(entity);
    }
    
    
   
}
