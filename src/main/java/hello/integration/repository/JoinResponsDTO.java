package hello.integration.repository;


import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinResponsDTO {

    private String nickname;
    private int characterId;
    private String modelPath;
}
