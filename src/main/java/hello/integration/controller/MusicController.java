package hello.integration.controller;

import hello.integration.repository.MusicStateDTO;
import hello.integration.service.MusicStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
@Controller
@Slf4j
public class MusicController {
    @Autowired
    private MusicStateService musicStateService;

    @MessageMapping("/music")
    @SendTo("/topic/music")
    public MusicStateDTO handleMusicState(MusicStateDTO musicState) {
        musicStateService.updateState(musicState);
        log.info("Music state updated by {}: {}", musicState.getPlayer(), musicState);
        return musicState;
    }
}
