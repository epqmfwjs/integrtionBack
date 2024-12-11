package hello.integration.controller;

import hello.integration.repository.MusicStateDTO;
import hello.integration.service.MusicStateService;
import hello.integration.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/npc")
@Slf4j
public class NpcController {

    @Autowired
    private NotificationService notificationService;
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
}
