package ucles.weblab.common.forms.domain;

import java.util.List;

/**
 *
 * @author Sukhraj
 */
public interface FormEntity extends Form {
    @Override
    List<String> getBusinessStreams();
    
    @Override
    String getApplicationName();
}
