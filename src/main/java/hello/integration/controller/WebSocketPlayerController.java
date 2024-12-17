package hello.integration.controller;

import hello.integration.repository.ChatMessage;
import hello.integration.repository.MusicStateDTO;
import hello.integration.repository.PlayerDTO;
import hello.integration.service.MemberService;
import hello.integration.service.MusicStateService;
import hello.integration.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Autowired
    private NotificationService notificationService;

    // 상수 정의
    private static final long RATE_LIMIT_NS = 16_000_000; // 16ms를 나노초로 변환
    private static final double POSITION_THRESHOLD = 0.01; // 1cm
    private static final double ROTATION_THRESHOLD = 0.01; // 약 0.57도
    private static final int POSITION_ARRAY_SIZE = 5; // x, y, z, rotation, timestamp

    private final Map<String, PlayerDTO> players = new ConcurrentHashMap<>();
    private final Map<String, String> sessionNicknames = new ConcurrentHashMap<>();
    private final Map<String, long[]> lastPositions = new ConcurrentHashMap<>();

    @MessageMapping("/position")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handlePosition(PlayerDTO player, SimpMessageHeaderAccessor headerAccessor) {
        String nickname = player.getNickname();
        long currentTime = System.nanoTime();

        long[] lastPosition = lastPositions.computeIfAbsent(nickname,
                k -> new long[POSITION_ARRAY_SIZE]);

        // Rate limiting check
        if (currentTime - lastPosition[4] < RATE_LIMIT_NS) {
            return null;
        }

        // 의미있는 움직임 체크
        if (!hasSignificantMovement(player, lastPosition)) {
            return null;
        }

        // 기존 플레이어 정보 유지
        PlayerDTO existingPlayer = players.get(nickname);
        if (existingPlayer != null) {

            player.setModelPath(existingPlayer.getModelPath());
            player.setCharacterId(existingPlayer.getCharacterId());
        }

        // 위치 데이터 업데이트
        updatePositionData(player, currentTime, lastPosition);

        player.setTimestamp(currentTime);
        players.put(nickname, player);
        return new HashMap<>(players);
    }

    private void updatePositionData(PlayerDTO player, long currentTime, long[] lastPosition) {
        lastPositions.put(player.getNickname(), new long[]{
                Double.doubleToLongBits(player.getPosition()[0]),
                Double.doubleToLongBits(player.getPosition()[1]),
                Double.doubleToLongBits(player.getPosition()[2]),
                Double.doubleToLongBits(player.getRotation()),
                currentTime
        });
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

    @MessageMapping("/join")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleJoin(PlayerDTO player, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        long currentTime = System.nanoTime();

        log.info("New player joined - Session ID: {}, Nickname: {}", sessionId, player.getNickname());

        // 초기 위치 데이터 설정
        long[] positionData = new long[POSITION_ARRAY_SIZE];
        updatePositionData(player, currentTime, positionData);

        player.setTimestamp(currentTime);
        sessionNicknames.put(sessionId, player.getNickname());
        players.put(player.getNickname(), player);

        // 현재 음악 상태 전송
        MusicStateDTO currentMusic = musicStateService.getCurrentState();
        if (currentMusic != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(),
                    "/user/queue/music",
                    currentMusic
            );
        }

        // 입장 알림 전송
        if (!player.getNickname().equals("관리자")) {
            String formattedDate = new SimpleDateFormat("yyyy.MM.dd HH:mm")
                    .format(new Date());
            notificationService.sendNotification(
                    "새로운 접속",
                    player.getNickname() + "님이 KwanghunWorld 에 입장했습니다! (" + formattedDate + ")"
            );
        }

        return new HashMap<>(players);
    }

    @MessageMapping("/leave")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleLeave(PlayerDTO player) {
        String nickname = player.getNickname();

        // 플레이어 데이터 정리
        players.remove(nickname);
        lastPositions.remove(nickname);
        memberService.deleteMember(nickname);

        // 퇴장 알림 전송
        if (!nickname.equals("관리자")) {
            String formattedDate = new SimpleDateFormat("yyyy.MM.dd HH:mm")
                    .format(new Date());
            notificationService.sendNotification(
                    "플레이어 종료",
                    nickname + "님이 KwanghunWorld에서 퇴장하셨습니다! (" + formattedDate + ")"
            );
        }

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