package Learning.Management.System.repository;

import Learning.Management.System.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepo extends JpaRepository<QuizAttempt, Long> {

    Optional<QuizAttempt> findByQuizIdAndLearnerId(Long quizId, Long learnerId);

    List<QuizAttempt> findByQuizIdOrderBySubmittedAtDesc(Long quizId);
}
