package rodrigue.dto;

import java.io.Serializable;

public class MessagePOJO implements Serializable {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
