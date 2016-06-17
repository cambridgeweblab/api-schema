
package ucles.weblab.common.forms.domain;

/**
 * Interface to create entity objects
 * 
 * @author Sukhraj
 */
public interface FormFactory {
   
    /**
     * Creates a new form entity
     * @param form
     * @return 
     */
    FormEntity newFormEntity(Form form);
    
}
