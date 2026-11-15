package Learning.Management.System.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "quiz_answers", uniqueConstraints = {
        @UniqueConstraint(name = "uq_quiz_answer_question", columnNames = {"attempt_id", "question_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuizQuestion question;

    @Column(name = "response_text", nullable = false, columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;
}
