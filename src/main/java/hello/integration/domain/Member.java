package hello.integration.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nickname;

    @Column
    private int characterId;

    @Column
    private String modelPath;

    @Builder

    public Member(String nickname, int characterId, String modelPath) {
        this.nickname = nickname;
        this.characterId = characterId;
        this.modelPath = modelPath;
    }
}
