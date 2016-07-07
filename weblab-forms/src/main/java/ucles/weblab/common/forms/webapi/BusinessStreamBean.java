package ucles.weblab.common.forms.webapi;

/**
 * A simple bean to encapsulate the name and abbreviation of the business streams. 
 * 
 * @author Sukhraj
 */
public class BusinessStreamBean {
   
    private final String abbreviation;
    private final String name;

    public BusinessStreamBean(String abbreviation, String name) {
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getName() {
        return name;
    }
    
}
