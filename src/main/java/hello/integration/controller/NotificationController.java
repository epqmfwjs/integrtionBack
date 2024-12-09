package hello.integration.controller;

import hello.integration.repository.TokenRegistrationRequest;
import hello.integration.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@Slf4j
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerToken(@RequestBody TokenRegistrationRequest request) {
        log.info("디버그: 토큰 등록 요청 받음 - userId: {}, token: {}", request.getUserId(), request.getToken());
        notificationService.registerToken(request.getUserId(), request.getToken());
        log.info("디버그: 현재 등록된 토큰 수: {}", notificationService.getCurrentTokens().size());
        return ResponseEntity.ok("Token registered successfully");
    }

    // 디버깅용 엔드포인트
    @GetMapping("/tokens")
    public ResponseEntity<Map<String, String>> getRegisteredTokens() {
        return ResponseEntity.ok(notificationService.getCurrentTokens());
    }

    // 테스트용 엔드포인트
    @PostMapping("/test")
    public ResponseEntity<String> sendTestNotification() {
        notificationService.sendNotification("Test Notification", "This is a test notification");
        return ResponseEntity.ok("Test notification sent");
    }

}