package Learning.Management.System.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "max_points", nullable = false)
    private Double maxPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 32)
    @Builder.Default
    private QuizQuestionType questionType = QuizQuestionType.WRITTEN;

    @Column(name = "correct_option_id")
    private Long correctOptionId;

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<QuizQuestionOption> options = new ArrayList<>();
}
