package hello.integration.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("kwanghyunworld-firebase-adminsdk-eufgg-9510f0e2f6.json").getInputStream());

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        // 이미 초기화되었는지 확인
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(firebaseOptions);
        }

        return FirebaseMessaging.getInstance();
    }
}
