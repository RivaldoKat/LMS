package Learning.Management.System.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "quiz_question_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private QuizQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;
}
