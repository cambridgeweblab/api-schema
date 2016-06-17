package ucles.weblab.common.forms.domain.mongo;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import ucles.weblab.common.forms.domain.Form;
import ucles.weblab.common.forms.domain.FormRepository;

/**
 *
 * @author Sukhraj
 */
public interface FormRepositoryMongo extends FormRepository, MongoRepository<Form, UUID> {
    
}
