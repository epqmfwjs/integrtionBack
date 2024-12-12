package hello.integration.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private FirebaseMessaging firebaseMessaging;
    @Value("${ADMIN_DEVICE_ID}")
    private String deviceId;
    private String currentToken = null;

    public void registerToken(String userId, String token) {
        log.info("Starting token registration - UserId: {}, Token: {}", userId, token);

        // deviceId가 다르면 디바이스의 토큰 등록 시도를 막음
        if (!deviceId.equals(userId)) {
            log.warn("Attempted to register token for non-admin device. UserId: {}", userId);
            return;
        }

        // 현재 토큰과 같다면 불필요한 업데이트 방지
        if (token.equals(currentToken)) {
            log.info("Token already registered and unchanged. Skipping update.");
            return;
        }

        // 새로운 토큰 저장
        currentToken = token;
        log.info("New token registered successfully for admin device");
    }

    public void sendNotification(String title, String body) {
        log.info("Attempting to send FCM notification - Title: '{}', Body: '{}'", title, body);

        if (currentToken == null) {
            log.warn("No FCM token registered. Cannot send notification.");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(currentToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Successfully sent FCM message to admin device. Response: {}", response);
        } catch (FirebaseMessagingException e) {
            if (isTokenInvalidError(e)) {
                log.warn("FCM token is invalid or expired. Clearing current token.");
                currentToken = null;
            }
            log.error("Failed to send FCM notification", e);
        }
    }

    private boolean isTokenInvalidError(FirebaseMessagingException e) {
        return e.getMessagingErrorCode() != null &&
                (e.getMessagingErrorCode().toString().equals("UNREGISTERED") ||
                        e.getMessagingErrorCode().toString().equals("INVALID_ARGUMENT"));
    }

    public boolean hasValidToken() {
        return currentToken != null;
    }

    // 필요한 경우 현재 토큰 정보 조회 (디버깅 등의 목적)
    public String getCurrentTokenInfo() {
        if (currentToken == null) {
            return "No token registered";
        }
        return "Token registered for admin device";
    }
}