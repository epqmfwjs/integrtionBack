package hello.integration.repository;

// PlayerDTO.java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

// PlayerDTO.java
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private String nickname;
    private double[] position = new double[3]; // 기본값으로 초기화
    private String currentAnimation;
    private double rotation;  // 회전 정보 추가
    private int characterId;
    private String modelPath;

}


