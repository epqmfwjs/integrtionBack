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
    private double[] position = new double[3];
    private String currentAnimation;
    private double rotation;
    private int characterId;
    private String modelPath;
    private long timestamp;
    private boolean needsNotification = true;
}


