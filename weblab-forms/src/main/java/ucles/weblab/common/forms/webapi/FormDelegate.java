package ucles.weblab.common.forms.webapi;

import org.springframework.beans.factory.annotation.Autowired;
import ucles.weblab.common.forms.domain.FormEntity;
import ucles.weblab.common.forms.domain.FormRepository;

/**
 * A delegate class sitting between controllers and repositories.
 * 
 * @author Sukhraj
 */
public class FormDelegate {
   
    private final FormRepository formRepository;
    private final FormResourceAssembler formAssembler; 
    
    
    @Autowired
    public FormDelegate(FormRepository formRepository,
                        FormResourceAssembler formAssembler) {
        this.formRepository = formRepository;
        this.formAssembler = formAssembler;                
    }
    
    public FormResource create(FormResource resource) {
        //call the factory to create one
        
        //repository.save(entity)
        
        //FormResource returned = toResource(savedEntity);
        
        //return returned;
        return null;
    }
    
    private FormResource toResource(FormEntity entity) {
        return formAssembler.toResource(entity);
    }
}
