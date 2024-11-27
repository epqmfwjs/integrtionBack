package hello.integration.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//public class SpotifyCallbackController {
//
//    @GetMapping("/callback")
//    public ResponseEntity<String> handleCallback(@RequestParam String code) {
//        log.info("Authorization code received: {}", code);
//        return ResponseEntity.ok("Authorization code received: " + code);
//    }
//}
// 리프레쉬 받아올테 테스트용으로 씀 일단 남겨둠