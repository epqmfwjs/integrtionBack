package hello.integration.repository;

import lombok.Data;

@Data
public class TokenRegistrationRequest {
    private String userId;
    private String token;
}