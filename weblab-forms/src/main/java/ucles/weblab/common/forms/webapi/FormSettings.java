package ucles.weblab.common.forms.webapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@ConfigurationProperties(prefix = "weblab.forms")
public class FormSettings {
    @NotNull
    @Size(min = 1)
    private Map<String, String> businessStream;

    public Map<String, String> getBusinessStream() {
        return businessStream;
    }

    public void setBusinessStream(Map<String, String> businessStream) {
        this.businessStream = businessStream;
    }
}
