package Learning.Management.System.repository;

import Learning.Management.System.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignmentRepo extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByCourseId(Long courseId, Pageable page);

    @Query("select a from Assignment a where exists "
            + "(select e.id from Enrollment e where e.course = a.course and e.student.id = :learnerId)")
    Page<Assignment> findVisibleToLearner(@Param("learnerId") Long learnerId, Pageable page);

    Optional<Assignment> findByCourseIdAndTitle(Long courseId, String title);

    @Query("select count(a) from Assignment a where exists "
            + "(select e.id from Enrollment e where e.course = a.course and e.student.id = :learnerId)")
    long countVisibleToLearner(@Param("learnerId") Long learnerId);
}
