package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizAnswerResponseDTO {
    private Long questionId;
    private String prompt;
    private Double maxPoints;
    private String responseText;
}
