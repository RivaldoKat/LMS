package Learning.Management.System.repository;


import Learning.Management.System.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;



@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);

    @Query("select c from Course c where exists "
            + "(select e.id from Enrollment e where e.course = c and e.student.id = :learnerId)")
    Page<Course> findEnrolledCourses(@Param("learnerId") Long learnerId, Pageable pageable);
}
