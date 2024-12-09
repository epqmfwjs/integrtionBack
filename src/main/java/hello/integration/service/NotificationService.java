package hello.integration.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private FirebaseMessaging firebaseMessaging;

    private final Map<String, String> userTokens = new ConcurrentHashMap<>();

    public void registerToken(String userId, String token) {
        log.info("Starting token registration - UserId: {}, Token: {}", userId, token);
        userTokens.put(userId, token);
        log.info("Token registered successfully. Current token count: {}", userTokens.size());
    }

    public void sendNotification(String title, String body) {
        log.info("Attempting to send FCM notification - Title: '{}', Body: '{}'", title, body);
        if (userTokens.isEmpty()) {
            log.warn("No FCM tokens registered. Cannot send notifications.");
            return;
        }

        for (Map.Entry<String, String> entry : userTokens.entrySet()) {
            String userId = entry.getKey();
            String token = entry.getValue();

            try {
                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();

                String response = firebaseMessaging.send(message);
                log.info("Successfully sent FCM message to userId: {}, Response: {}",
                        userId, response);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send FCM notification", e);
            }
        }
    }

    public Map<String, String> getCurrentTokens() {
        return new HashMap<>(userTokens);
    }
}