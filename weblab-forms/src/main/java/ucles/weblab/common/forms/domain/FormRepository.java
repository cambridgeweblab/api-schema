package ucles.weblab.common.forms.domain;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Sukhraj
 */
public interface FormRepository {
    
    <S extends FormEntity> S save(S s);

    List<? extends FormEntity> findAllByOrderByNameAsc();

    Optional<? extends FormEntity> findOne(String id);

    void delete(FormEntity form);
    
    List<? extends FormEntity> findByBusinessStreamsContainingAndApplicationName(String businessStream, String applicationName);
}
