package hello.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS 설정
        registry.addMapping("/**")  // API 엔드포인트에 대한 패턴
                //.allowedOrigins("http://localhost:3000","http://localhost:5000" ,"http://192.168.219.186:5000","http://localhost:19006","exp://192.168.219.186:19000","exp://localhost:19000")  // React 앱의 주소
                .allowedOrigins(
                        "http://gogolckh.ddns.net:8010",
                        "http://gogolckh.ddns.net:10",
                        "https://gogolckh.ddns.net:8010",
                        "https://gogolckh.ddns.net:10",
                        "https://gogolckh.ddns.net:8443"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메서드
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true)  // 인증 정보 허용
                .maxAge(3600);  // 캐시 시간
    }
}
