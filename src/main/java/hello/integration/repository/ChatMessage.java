package hello.integration.repository;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Data
@ToString
public class ChatMessage {
    private String nickname;
    private String message;
    private Long timestamp;
}
