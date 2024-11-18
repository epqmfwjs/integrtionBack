package hello.integration.controller;

import hello.integration.repository.ChatMessage;
import hello.integration.repository.PlayerDTO;
import hello.integration.domain.MemberRepository;  // 추가
import hello.integration.service.MemberService;       // 추가
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class WebSocketPlayerController {

    private final Map<String, PlayerDTO> players = new ConcurrentHashMap<>();
    private final Map<String, String> sessionNicknames = new ConcurrentHashMap<>();

    @Autowired
    private MemberService memberService;  // 추가

    @MessageMapping("/position")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handlePosition(PlayerDTO player, SimpMessageHeaderAccessor headerAccessor) {
        //System.out.println("보낼 플레이어 정보 : " + player.toString());
        String sessionId = headerAccessor.getSessionId();
        players.put(player.getNickname(), player);
        return new HashMap<>(players);
    }

    @MessageMapping("/join")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleJoin(PlayerDTO player, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        //System.out.println("받은 플레이어 정보 : " + player.toString());
        log.info("New player joined - Session ID: {}, Nickname: {}", sessionId, player.getNickname());

        sessionNicknames.put(sessionId, player.getNickname());
        players.put(player.getNickname(), player);

        return new HashMap<>(players);
    }

    @MessageMapping("/leave")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleLeave(PlayerDTO player) {
        log.info("Leave 요청 받음 - nickname: {}", player.getNickname());
        log.info("현재 접속자 수: {}", players.size());
        players.remove(player.getNickname());

        // DB에서 플레이어 삭제
        memberService.deleteMember(player.getNickname());

        log.info("제거 후 접속자 수: {}", players.size());
        return new HashMap<>(players);
    }

    // 세션 ID로 닉네임 조회
    public String getNicknameBySessionId(String sessionId) {
        return sessionNicknames.get(sessionId);
    }

    // 세션 제거 및 플레이어 제거
    public Map<String, PlayerDTO> removeBySessionId(String sessionId) {
        String nickname = sessionNicknames.remove(sessionId);
        if (nickname != null) {
            players.remove(nickname);

            // DB에서 플레이어 삭제
            try {
                memberService.deleteMember(nickname);
                log.info("Player removed from DB: {}", nickname);
            } catch (Exception e) {
                log.error("Error removing player from DB: {}", nickname, e);
            }
            log.info("Session disconnected - removed player: {}", nickname);
        }
        return new HashMap<>(players);
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public ChatMessage handleChat(ChatMessage message) {
        //System.out.println("들어온 챗메세지는? :" + message.toString());
        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis());
        }
        //System.out.println("나가는 챗메세지는? :" + message.toString());
        return message;
    }
}