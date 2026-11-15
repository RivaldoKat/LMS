package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizQuestionResponseDTO {
    private Long id;
    private String prompt;
    private Double maxPoints;
    private Learning.Management.System.entity.QuizQuestionType questionType;
    private List<QuizOptionResponseDTO> options;
    private Long correctOptionId;
}
