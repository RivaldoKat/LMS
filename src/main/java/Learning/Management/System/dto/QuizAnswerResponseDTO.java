package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizAnswerResponseDTO {
    private Long questionId;
    private String prompt;
    private Double maxPoints;
    private Learning.Management.System.entity.QuizQuestionType questionType;
    private String responseText;
    private Long selectedOptionId;
    private String selectedOptionText;
    private Long correctOptionId;
    private String correctOptionText;
}
