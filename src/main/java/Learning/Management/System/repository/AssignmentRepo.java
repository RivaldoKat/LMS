package Learning.Management.System.repository;

import Learning.Management.System.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepo extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseId(Long courseId);
}
