package Learning.Management.System.repository;

import Learning.Management.System.entity.Submission;
import Learning.Management.System.entity.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepo extends JpaRepository<Submission, Long> {

    Page<Submission> findByStudentId(Long studentId, Pageable pageable);

    Page<Submission> findByStudentIdAndAssignmentCourseTeacherId(
            Long studentId, Long teacherId, Pageable pageable);

    Page<Submission> findByStudentIdAndStatus(
            Long studentId, SubmissionStatus status, Pageable pageable);

    Page<Submission> findByAssignmentCourseTeacherIdAndStatus(
            Long teacherId, SubmissionStatus status, Pageable pageable);

    Page<Submission> findByStatus(SubmissionStatus status, Pageable pageable);

    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
}
