package hello.integration.service;

import hello.integration.repository.SpotifyTokenDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyAuthService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.refresh.token}")
    private String refreshToken;

    private String accessToken;
    private LocalDateTime tokenExpirationTime;

    private final RestTemplate restTemplate;

    @PostConstruct
    public void initialize() {
        log.info("Initializing SpotifyAuthService with:");
        log.info("Client ID: {}", clientId);
        log.info("Client Secret: {}", clientSecret);
        log.info("Refresh Token: {}", refreshToken);
        refreshAccessToken();
    }

    private void refreshAccessToken() {
        try {
            // Base64로 인코딩된 인증 문자열 생성
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            log.info("Encoded credentials: {}", encodedCredentials);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Authorization", "Basic " + encodedCredentials);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            log.info("Sending token refresh request to Spotify...");
            ResponseEntity<SpotifyTokenDTO> response = restTemplate.exchange(
                    "https://accounts.spotify.com/api/token",
                    HttpMethod.POST,
                    request,
                    SpotifyTokenDTO.class
            );

            if (response.getBody() != null) {
                this.accessToken = response.getBody().getAccessToken();
                this.tokenExpirationTime = LocalDateTime.now()
                        .plusSeconds(response.getBody().getExpiresIn());
                log.info("Successfully refreshed access token");
            }
        } catch (Exception e) {
            log.error("Error details during token refresh:", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    public synchronized String getValidAccessToken() {
        if (accessToken == null || LocalDateTime.now().plusMinutes(1).isAfter(tokenExpirationTime)) {
            refreshAccessToken();
        }
        return accessToken;
    }
}
