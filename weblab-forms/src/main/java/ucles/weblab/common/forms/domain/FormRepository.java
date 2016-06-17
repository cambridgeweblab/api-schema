package ucles.weblab.common.forms.domain;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Sukhraj
 */
public interface FormRepository {
    
    <S extends Form> S save(S s);

    List<Form> findAllByOrderByNameAsc();

    Form findOne(UUID id);

    void delete(Form form);
}
