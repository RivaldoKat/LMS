package Learning.Management.System.repository;

import Learning.Management.System.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepo extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);
}
