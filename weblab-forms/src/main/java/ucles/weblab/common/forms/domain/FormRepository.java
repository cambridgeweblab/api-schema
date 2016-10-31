package ucles.weblab.common.forms.domain;

import java.util.List;

/**
 *
 * @author Sukhraj
 */
public interface FormRepository {

    <S extends FormEntity> S save(S s);

    List<? extends FormEntity> findAllByOrderByNameAsc();

    <S extends FormEntity> S findOne(String id);

    int deleteById(String id);

    List<? extends FormEntity> findByBusinessStreamsContainingAndApplicationName(String businessStream, String applicationName);
}
