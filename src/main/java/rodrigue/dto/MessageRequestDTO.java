package rodrigue.dto;

import java.io.Serializable;

public class MessageRequestDTO implements Serializable {
    private final String id;
    private final String payload;

    public MessageRequestDTO(String id, String payload) {
        this.id = id;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }
}
