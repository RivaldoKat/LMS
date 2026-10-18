package Learning.Management.System.repository;

import Learning.Management.System.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;


@Repository
public interface AssignmentRepo extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findByCourseId(Long courseId);
}
