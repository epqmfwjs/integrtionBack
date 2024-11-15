// WebSocketPlayerController.java
package hello.integration.controller;


import hello.integration.repository.PlayerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// WebSocketController.java
@Slf4j
@Controller
public class WebSocketPlayerController {

    private final Map<String, PlayerDTO> players = new ConcurrentHashMap<>();

    @MessageMapping("/position")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handlePosition(PlayerDTO player) {

        //System.out.println("들어온 행동은? : " + player.toString());
        //log.info("Position update - Player: {} Position: {}",
                //player.getNickname(),
                //Arrays.toString(player.getPosition()));

        players.put(player.getNickname(), player);

        //log.info("All players after update: {}", players);
        return new HashMap<>(players); // 새로운 Map 인스턴스 반환
    }

    @MessageMapping("/join")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleJoin(PlayerDTO player) {
        //log.info("New player joined: {}", player);
        players.put(player.getNickname(), player);
        //log.info("All players after join: {}", players);
        return new HashMap<>(players);
    }

    @MessageMapping("/leave")
    @SendTo("/topic/players")
    public Map<String, PlayerDTO> handleLeave(PlayerDTO player) {
        //log.info("Player leaving: {}", player.getNickname());
        players.remove(player.getNickname());
        //log.info("Remaining players: {}", players);
        return new HashMap<>(players);
    }
}
