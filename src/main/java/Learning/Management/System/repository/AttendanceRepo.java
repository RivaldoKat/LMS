package Learning.Management.System.repository;

import Learning.Management.System.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AttendanceRepo extends JpaRepository<Attendance, Long> {
    Page<Attendance> findByCourseId(Long courseId, Pageable page);

    boolean existsByCourseIdAndStudentIdAndDate(Long courseId, Long studentId, LocalDate date);
}
