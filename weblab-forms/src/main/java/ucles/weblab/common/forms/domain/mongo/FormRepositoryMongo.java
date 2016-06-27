package ucles.weblab.common.forms.domain.mongo;

import java.util.UUID;
import org.springframework.data.repository.Repository;
import ucles.weblab.common.forms.domain.FormEntity;
import ucles.weblab.common.forms.domain.FormRepository;

/**
 *
 * @author Sukhraj
 */
public interface FormRepositoryMongo extends FormRepository, Repository<FormEntity, UUID> {
    
}
