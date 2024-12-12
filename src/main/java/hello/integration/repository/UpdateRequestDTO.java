// UpdateRequestDTO.java
package hello.integration.repository;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UpdateRequestDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime date;

    private String title;

    private String content;

    // 포맷된 날짜 문자열을 반환하는 메서드
    public String getFormattedDate() {
        if (date == null) return "";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }
}
