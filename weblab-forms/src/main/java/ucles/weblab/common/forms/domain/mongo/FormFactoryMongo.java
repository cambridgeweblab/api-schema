package ucles.weblab.common.forms.domain.mongo;

import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormEntity;
import ucles.weblab.common.forms.domain.FormFactory;

/**
 *
 * @author Sukhraj
 */
public class FormFactoryMongo implements FormFactory {

    @Override
    public FormEntity newFormEntity(Form form) {
        return new FormEntityMongo(form);
    }
    
}
