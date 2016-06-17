package ucles.weblab.common.forms.webapi;

import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormEntity;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.FormRepository;
import ucles.weblab.common.forms.domain.ImmutableForm;

/**
 * A delegate class sitting between controllers and repositories.
 * 
 * @author Sukhraj
 */
public class FormDelegate {
   
    private final FormRepository formRepository;
    private final FormResourceAssembler formAssembler; 
    private final FormFactory formFactory;

    @Autowired
    public FormDelegate(FormRepository formRepository,
                        FormResourceAssembler formAssembler,
                        FormFactory formFactory) {
        this.formRepository = formRepository;
        this.formAssembler = formAssembler;    
        this.formFactory = formFactory;
    }
    
    public FormResource create(FormResource resource) {
        
        Form vo = ImmutableForm.builder()
                                .applicationName(resource.getApplicationName())
                                .businessStream(resource.getBusinessStream())
                                .name(resource.getName())
                                .schema(resource.getSchema())
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
