package ucles.weblab.common.forms.domain.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ucles.weblab.common.forms.domain.FormRepository;

/**
 *
 * @author Sukhraj
 */
public interface FormRepositoryMongo extends FormRepository, MongoRepository<FormEntityMongo, String> {
    
    @Override
    FormEntityMongo findOne(String id);
}
