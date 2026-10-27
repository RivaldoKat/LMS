package Learning.Management.System.repository;

import Learning.Management.System.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepo extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourseId(Long courseId);
}
