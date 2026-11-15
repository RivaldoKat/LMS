package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizOptionResponseDTO {
    private Long id;
    private String text;
}
