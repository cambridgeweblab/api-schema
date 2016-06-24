package ucles.weblab.common.forms.exception;

import org.springframework.core.NestedRuntimeException;
import java.io.Serializable;

/**
 *
 * @author Sukhraj
 */
public class BadDataException extends NestedRuntimeException {
   
    private final Serializable data;

    public BadDataException(String msg, Serializable data) {
        super(msg);
        this.data = data;
    }

    public BadDataException(String msg, Serializable data, Throwable cause) {
        super(msg, cause);
        this.data = data;
    }

    public Serializable getData() {
        return data;
    }
}

