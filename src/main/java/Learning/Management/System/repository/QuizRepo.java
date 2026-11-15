package Learning.Management.System.repository;

import Learning.Management.System.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepo extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCourseTeacherIdOrderByOpensAtAsc(Long teacherId);

    @Query("select q from Quiz q where exists "
            + "(select e.id from Enrollment e where e.course = q.course and e.student.id = :learnerId) "
            + "order by q.opensAt asc")
    List<Quiz> findVisibleToLearner(@Param("learnerId") Long learnerId);
}
