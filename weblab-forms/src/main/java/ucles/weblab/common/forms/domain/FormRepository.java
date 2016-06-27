package ucles.weblab.common.forms.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author Sukhraj
 */
public interface FormRepository {
    
    <S extends FormEntity> S save(S s);

    List<? extends FormEntity> findAllByOrderByNameAsc();

    Optional<? extends FormEntity> findOne(UUID id);

    void delete(FormEntity form);
    
    List<? extends FormEntity> findOneByBusinessStreamAndApplicationName(String businessName, String applicationName);
}
