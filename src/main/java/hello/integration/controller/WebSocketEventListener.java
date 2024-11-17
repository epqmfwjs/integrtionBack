package hello.integration.controller;

import hello.integration.repository.PlayerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketPlayerController playerController;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate,
                                  WebSocketPlayerController playerController) {
        this.messagingTemplate = messagingTemplate;
        this.playerController = playerController;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            log.info("Client disconnected: {}", sessionId);
            Map<String, PlayerDTO> remainingPlayers = playerController.removeBySessionId(sessionId);
            messagingTemplate.convertAndSend("/topic/players", remainingPlayers);
        }
    }
}