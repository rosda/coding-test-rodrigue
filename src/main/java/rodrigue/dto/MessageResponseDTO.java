package rodrigue.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponseDTO implements Serializable {
    private final int code;
    private final String success;
    private final String text;

    public MessageResponseDTO() {
        this.code = 0;
        this.success = null;
        this.text = null;
    }

    public MessageResponseDTO(int code, String success, String text) {
        this.code = code;
        this.success = success;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getSuccess() {
        return success;
    }

    public String getText() {
        return text;
    }
}
