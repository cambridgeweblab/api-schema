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

    Optional<? extends FormEntity> findById(String id);

    boolean existsById(String id);

    void deleteById(String id);

    List<? extends FormEntity> findByBusinessStreamsContainingAndApplicationName(String businessStream, String applicationName);
}
