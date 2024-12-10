package hello.integration.repository;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class JoinResponsDTO {

    private String nickname;
    private int characterId;
    private String modelPath;
}
