package hello.integration.repository;


import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NicknameCheckResponseDTO {

    private boolean available;
}
