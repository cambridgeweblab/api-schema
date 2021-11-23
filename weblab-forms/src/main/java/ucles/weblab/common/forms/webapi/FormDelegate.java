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
import ucles.weblab.common.webapi.exception.BadDataException;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;

import static java.util.stream.Collectors.toList;

/**
 * A delegate class sitting between controllers and repositories.
 *
 * @author Sukhraj
 */
public class FormDelegate {

    private static final Logger log = LoggerFactory.getLogger(FormDelegate.class);
    private static final String CONVERSION_ERROR = "Can not convert schema to a string";

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
        String stringValue;
        try {
            stringValue = objectMapper.writeValueAsString(resource.getFormDefinition());
        } catch (JsonProcessingException ex) {
            log.error(CONVERSION_ERROR, ex);
            throw new BadDataException(CONVERSION_ERROR, null, ex);
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

        return toResource(saved);

    }

    public FormResource get(String id) {

        FormEntity formEntity = formRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        return toResource(formEntity);
    }

    public void delete(String id) {
        if (formRepository.existsById(id)) {
            formRepository.deleteById(id);
        }
        throw new ResourceNotFoundException(id);
    }

    public List<FormResource> list(String businessStream, String applicationName) {
        List<? extends FormEntity> entities = formRepository.findByBusinessStreamsContainingAndApplicationName(businessStream, applicationName);

        return entities.stream().map(formAssembler::toModel).collect(toList());
    }

    public FormResource update(FormResource resource) {
        FormEntity existingEntity = formRepository.findById(resource.getFormId())
                .orElseThrow(() -> new ResourceNotFoundException(resource.getFormId()));
        String stringValue;
        try {
            stringValue = objectMapper.writeValueAsString(resource.getFormDefinition());
        } catch (JsonProcessingException ex) {
            log.error(CONVERSION_ERROR, ex);
            throw new BadDataException(CONVERSION_ERROR, null, ex);
        }

        existingEntity.setDescription(resource.getDescription());
        existingEntity.setName(resource.getName());
        existingEntity.setApplicationName(resource.getApplicationName());
        existingEntity.setBusinessStreams(resource.getBusinessStreams());
        existingEntity.setDescription(resource.getDescription());
        existingEntity.setSchema(stringValue);
        existingEntity.setValidFrom(resource.getValidFrom());
        existingEntity.setValidTo(resource.getValidTo());

        FormEntity saved = formRepository.save(existingEntity);

        return toResource(saved);
    }


    private FormResource toResource(FormEntity entity) {
        return formAssembler.toModel(entity);
    }
}
