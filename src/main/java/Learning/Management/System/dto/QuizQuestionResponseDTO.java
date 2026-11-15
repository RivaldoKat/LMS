package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizQuestionResponseDTO {
    private Long id;
    private String prompt;
    private Double maxPoints;
}
