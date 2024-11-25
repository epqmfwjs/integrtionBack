package hello.integration.repository;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Data
@ToString
public class MusicStateDTO {
    private String player;      // 재생/일시정지를 한 플레이어
    private String action;      // 'play', 'pause', 'trackChange'
    private boolean isPlaying;  // 재생 중인지 여부
    private int trackIndex;     // 현재 트랙 인덱스
    private double currentTime; // 현재 재생 시간

}
