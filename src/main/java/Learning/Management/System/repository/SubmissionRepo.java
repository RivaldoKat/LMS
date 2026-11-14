package Learning.Management.System.repository;

import Learning.Management.System.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepo extends JpaRepository<Submission, Long> {

    Page<Submission> findByStudentId(Long studentId, Pageable pageable);

    Page<Submission> findByStudentIdAndAssignmentCourseTeacherId(
            Long studentId, Long teacherId, Pageable pageable);

    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
}
