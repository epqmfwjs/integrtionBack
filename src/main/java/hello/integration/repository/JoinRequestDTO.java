package hello.integration.repository;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JoinRequestDTO {

    private String nickname;
    private int characterId;
    private String modelPath;
}
