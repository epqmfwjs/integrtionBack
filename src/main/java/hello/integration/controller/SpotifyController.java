package hello.integration.controller;

import hello.integration.service.SpotifyAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"}) // React 앱의 주소
@Slf4j
public class SpotifyController {

    private final SpotifyAuthService spotifyAuthService;
    private final RestTemplate restTemplate;

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken() {
//        log.info("Token requested");
        String token = spotifyAuthService.getValidAccessToken();
        return ResponseEntity.ok(Map.of("access_token", token));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String code) {
//        log.info("Authorization code received: {}", code);
        return ResponseEntity.ok("Authorization code received: " + code);
    }

    @GetMapping("/player/current-playback")
    public ResponseEntity<String> getCurrentPlayback() {
//        log.info("Current playback requested");
        String token = spotifyAuthService.getValidAccessToken();
        return proxySpotifyRequest("me/player", HttpMethod.GET, token, null);
    }

    @PutMapping("/player/play")
    public ResponseEntity<String> playTrack(@RequestParam String device_id, @RequestBody String requestBody) {
//        log.info("Play track requested for device: {}", device_id);
        String token = spotifyAuthService.getValidAccessToken();
        return proxySpotifyRequest("me/player/play?device_id=" + device_id, HttpMethod.PUT, token, requestBody);
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchTracks(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
//        log.info("Search requested for query: {}, limit: {}", query, limit);
        String token = spotifyAuthService.getValidAccessToken();
        return proxySpotifyRequest(
                "search?q=" + query + "&type=track&limit=" + limit + "&market=KR" + "&include_external=audio" + "&locale=ko_KR", // 한국 마켓 추가
                HttpMethod.GET,
                token,
                null
        );
    }

    private ResponseEntity<String> proxySpotifyRequest(String path, HttpMethod method, String token, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
            String url = "https://api.spotify.com/v1/" + path;

//            log.info("Proxying request to Spotify API: {} {}", method, url);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    requestEntity,
                    String.class
            );
//            log.info("Received response from Spotify API with status: {}", response.getStatusCode());

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
//            log.error("Error from Spotify API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
//            log.error("Unexpected error while proxying request to Spotify API", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal Server Error\"}");
        }
    }
}