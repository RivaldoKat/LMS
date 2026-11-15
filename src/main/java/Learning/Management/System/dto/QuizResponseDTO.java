package Learning.Management.System.dto;

import Learning.Management.System.entity.QuizAttemptStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class QuizResponseDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private OffsetDateTime opensAt;
    private OffsetDateTime closesAt;
    private Double totalPoints;
    private Boolean hasMultipleChoiceQuestions;
    private Boolean hasWrittenQuestions;
    private List<QuizQuestionResponseDTO> questions;
    private Long myAttemptId;
    private QuizAttemptStatus myAttemptStatus;
    private Double myGrade;
    private Double myAutoGradedPoints;
    private String myFeedback;
    private List<QuizAnswerResponseDTO> myAnswers;
}
