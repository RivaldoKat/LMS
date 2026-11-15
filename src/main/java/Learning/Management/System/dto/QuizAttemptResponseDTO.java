package Learning.Management.System.dto;

import Learning.Management.System.entity.QuizAttemptStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuizAttemptResponseDTO {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long learnerId;
    private String learnerName;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private Double grade;
    private String feedback;
    private QuizAttemptStatus status;
    private List<QuizAnswerResponseDTO> answers;
}
