package hello.integration.controller;

import hello.integration.repository.ChatMessage;
import hello.integration.repository.MusicStateDTO;
import hello.integration.repository.PlayerDTO;
import hello.integration.service.MemberService;
import hello.integration.service.MusicStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class WebSocketPlayerController {

    @Autowired
    private MusicStateService musicStateService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private MemberService memberService;

    // 상수 정의
    private static final long RATE_LIMIT_MS = 16; // 약 60fps
    private static final double POSITION_THRESHOLD = 0.01; // 1cm
    private static final double ROTATION_THRESHOLD = 0.01; // 약 0.57도

    private static final int POSITION_ARRAY_SIZE = 5; // x, y, z, rotation, timestamp

    private final Map<String, PlayerDTO> players = new ConcurrentHashMap<>();
    private final Map<String, String> sessionNicknames = new ConcurrentHashMap<>();
    private final Map<String, long[]> lastPositions = new ConcurrentHashMap<>();


// -------------------------밑으로 기존코드 -------------------------------------

//    @MessageMapping("/position")
//    @SendTo("/topic/players")
//    public Map<String, PlayerDTO> handlePosition(PlayerDTO player, SimpMessageHeaderAccessor headerAccessor) {
//        String nickname = player.getNickname();
//        long currentTime = System.currentTimeMillis();
//
//
//        // 기존 플레이어 정보 가져오기
//        PlayerDTO existingPlayer = players.get(nickname);
//        if (existingPlayer != null) {
//            // 기존 정보 유지하면서 새로운 정보 업데이트
//            player.setModelPath(existingPlayer.getModelPath());
//            player.setCharacterId(existingPlayer.getCharacterId());
//        }
//
//        player.setTimestamp(currentTime);
//
//        // Rate limiting check
//        long[] lastPosition = lastPositions.get(nickname);
//
//        if (lastPosition != null) {
//            if (currentTime - lastPosition[3] < RATE_LIMIT_MS) {
//                return null;
//            }
//
//            if (!hasSignificantChange(
//                    new double[]{lastPosition[0], lastPosition[1], lastPosition[2]},
//                    player.getPosition())) {
//                return null;
//            }
//        }
//
//        lastPositions.put(nickname, new long[]{
//                Double.doubleToLongBits(player.getPosition()[0]),
//                Double.doubleToLongBits(player.getPosition()[1]),
//                Double.doubleToLongBits(player.getPosition()[2]),
//                currentTime
//        });
//
//        players.put(nickname, player);
//        System.out.println("포지션 메시지 : " + player.toString());
//        log.debug("Players map content: {}", players);
//        return new HashMap<>(players);
//    }
//----------------------------------위로는 기존코드-----------------------------------------


//-----------------------------------------밑으로는 수정코드------------------------------------------------------

    @MessageMapping("/position")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handlePosition(PlayerDTO player, SimpMessageHeaderAccessor headerAccessor) {
        String nickname = player.getNickname();
        long currentTime = System.nanoTime();

        // 배열이 없거나 크기가 잘못된 경우 새로 초기화
        if (lastPositions.get(nickname) == null ||
                lastPositions.get(nickname).length != POSITION_ARRAY_SIZE) {
            lastPositions.put(nickname, new long[POSITION_ARRAY_SIZE]);
        }

        long[] lastPosition = lastPositions.get(nickname);

        // Rate limiting check
        if (currentTime - lastPosition[4] < RATE_LIMIT_MS) {
            return null;
        }

        // 새로운 위치 데이터 저장
        lastPosition[0] = Double.doubleToLongBits(player.getPosition()[0]);
        lastPosition[1] = Double.doubleToLongBits(player.getPosition()[1]);
        lastPosition[2] = Double.doubleToLongBits(player.getPosition()[2]);
        lastPosition[3] = Double.doubleToLongBits(player.getRotation());
        lastPosition[4] = currentTime;

        // 기존 플레이어 정보 유지
        PlayerDTO existingPlayer = players.get(nickname);
        if (existingPlayer != null) {
            player.setModelPath(existingPlayer.getModelPath());
            player.setCharacterId(existingPlayer.getCharacterId());
        }

        // Rate limiting과 위치 변화 확인
//        long[] lastPosition = lastPositions.get(nickname);
//        if (lastPosition != null) {
//            if ((currentTime - lastPosition[4]) / 1_000_000 < RATE_LIMIT_MS) {
//                return null;
//            }
//
//            if (!hasSignificantMovement(player, lastPosition)) {
//                return null;
//            }
//        }

        // 5개 요소를 가진 배열로 수정 (x, y, z, rotation, timestamp)
        lastPositions.put(nickname, new long[]{
                Double.doubleToLongBits(player.getPosition()[0]),
                Double.doubleToLongBits(player.getPosition()[1]),
                Double.doubleToLongBits(player.getPosition()[2]),
                Double.doubleToLongBits(player.getRotation()),
                currentTime
        });

        player.setTimestamp(currentTime);
        players.put(nickname, player);
        return new HashMap<>(players);
    }

    private boolean hasSignificantMovement(PlayerDTO newPlayer, long[] lastPosition) {
        if (lastPosition == null) return true;

        double[] oldPos = new double[]{
                Double.longBitsToDouble(lastPosition[0]),
                Double.longBitsToDouble(lastPosition[1]),
                Double.longBitsToDouble(lastPosition[2])
        };
        double oldRotation = Double.longBitsToDouble(lastPosition[3]);

        return hasSignificantPositionChange(oldPos, newPlayer.getPosition()) ||
                Math.abs(newPlayer.getRotation() - oldRotation) > ROTATION_THRESHOLD;
    }

    private boolean hasSignificantPositionChange(double[] oldPos, double[] newPos) {
        for (int i = 0; i < 3; i++) {
            if (Math.abs(oldPos[i] - newPos[i]) > POSITION_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

//-----------------------------------------위로는 수정코드------------------------------------------------------
    private boolean hasSignificantChange(double[] oldPos, double[] newPos) {
        return Math.abs(oldPos[0] - newPos[0]) > POSITION_THRESHOLD ||
                Math.abs(oldPos[1] - newPos[1]) > POSITION_THRESHOLD ||
                Math.abs(oldPos[2] - newPos[2]) > POSITION_THRESHOLD;
    }

    @MessageMapping("/join")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleJoin(PlayerDTO player, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("조인메시지 : " + player.toString());
        String sessionId = headerAccessor.getSessionId();
        long currentTime = System.currentTimeMillis();
        player.setTimestamp(currentTime);
        log.info("New player joined - Session ID: {}, Nickname: {}", sessionId, player.getNickname());

        // 5개 요소를 가진 배열로 초기화------------------------------수정(추가)
        lastPositions.put(player.getNickname(), new long[POSITION_ARRAY_SIZE]);
        lastPositions.get(player.getNickname())[0] = Double.doubleToLongBits(player.getPosition()[0]);
        lastPositions.get(player.getNickname())[1] = Double.doubleToLongBits(player.getPosition()[1]);
        lastPositions.get(player.getNickname())[2] = Double.doubleToLongBits(player.getPosition()[2]);
        lastPositions.get(player.getNickname())[3] = Double.doubleToLongBits(player.getRotation());
        lastPositions.get(player.getNickname())[4] = System.currentTimeMillis();

        // 위로는 수정코드---------

        // Send current music state
        MusicStateDTO currentMusic = musicStateService.getCurrentState();
        if (currentMusic != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(),
                    "/user/queue/music",
                    currentMusic
            );
        }

        sessionNicknames.put(sessionId, player.getNickname());
        players.put(player.getNickname(), player);
        lastPositions.put(player.getNickname(), new long[]{
                Double.doubleToLongBits(player.getPosition()[0]),
                Double.doubleToLongBits(player.getPosition()[1]),
                Double.doubleToLongBits(player.getPosition()[2]),
                System.currentTimeMillis()
        });
        log.debug("Current players after join: {}", players);
        return new HashMap<>(players);
    }

    @MessageMapping("/leave")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleLeave(PlayerDTO player) {
        String nickname = player.getNickname();
        players.remove(nickname);
        lastPositions.remove(nickname);
        memberService.deleteMember(nickname);
        return new HashMap<>(players);
    }

    public Map<String, PlayerDTO> removeBySessionId(String sessionId) {
        String nickname = sessionNicknames.remove(sessionId);
        if (nickname != null) {
            players.remove(nickname);
            lastPositions.remove(nickname);
            try {
                memberService.deleteMember(nickname);
                log.info("Player removed: {}", nickname);
            } catch (Exception e) {
                log.error("Error removing player: {}", nickname, e);
            }
        }
        return new HashMap<>(players);
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public ChatMessage handleChat(ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis());
        }
        return message;
    }
}