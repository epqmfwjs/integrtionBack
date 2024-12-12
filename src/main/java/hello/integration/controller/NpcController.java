package hello.integration.controller;

import hello.integration.repository.MusicStateDTO;
import hello.integration.repository.UpdateRequestDTO;
import hello.integration.service.MusicStateService;
import hello.integration.service.NotificationService;
import hello.integration.service.UpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/npc")
@Slf4j
public class NpcController {

    private final UpdateService updateService;
    private final NotificationService notificationService;

    @Autowired
    public NpcController(UpdateService updateService, NotificationService notificationService) {
        this.updateService = updateService;
        this.notificationService = notificationService;
    }

    @GetMapping("/adminCall")
    public String adminCall(@RequestParam("playerNickname") String playerNickname) {

        System.out.println("adminCall !!!! : " + playerNickname + " 님의 호출입니다");
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        String formattedDate = dateFormat.format(now);

        notificationService.sendNotification(
                "관리자 호출",
                playerNickname + "님의 관리자 호출 입니다.(" + formattedDate + ")"
        );

        return "관리자를 호출 하였습니다. (App PUSH)";
    }

    @GetMapping("/getUpdates")
    public ResponseEntity<List<UpdateRequestDTO>> getUpdates() {
        try {
            List<UpdateRequestDTO> updates = updateService.getAllUpdatesSortedByDateDesc();
            return ResponseEntity.ok(updates);
        } catch (Exception e) {
            log.error("Error fetching updates: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/setUpdate")
    public ResponseEntity<UpdateRequestDTO> createUpdate(@RequestBody UpdateRequestDTO updateDTO) {
        try {
            UpdateRequestDTO created = updateService.createUpdate(updateDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating update: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/deleteUpdate/{id}")
    public ResponseEntity<Void> deleteUpdate(@PathVariable Long id) {
        try {
            updateService.deleteUpdate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting update: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateUpdate/{id}")
    public ResponseEntity<UpdateRequestDTO> updateUpdate(
            @PathVariable Long id,
            @RequestBody UpdateRequestDTO updateDTO
    ) {
        try {
            UpdateRequestDTO updated = updateService.updateUpdate(id, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating update: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
