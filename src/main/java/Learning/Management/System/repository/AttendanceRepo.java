package Learning.Management.System.repository;

import Learning.Management.System.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepo extends JpaRepository<Attendance, Long> {
    List<Attendance> findByCourseId(Long courseId);
    List<Attendance> findByStudentId(Long studentId);
}
