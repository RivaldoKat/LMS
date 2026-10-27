package Learning.Management.System.service;

import Learning.Management.System.dto.AttendanceRequestDTO;
import Learning.Management.System.dto.AttendanceResponseDTO;
import Learning.Management.System.model.Attendance;
import Learning.Management.System.model.AttendanceStatus;
import Learning.Management.System.model.Course;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.AttendanceRepo;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepo attendanceRepo;
    private final CourseRepo courseRepo;
    private final UserRepo userRepo;

    public List<AttendanceResponseDTO> getAttendanceByCourse(Long courseId) {
        return attendanceRepo.findByCourseId(courseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AttendanceResponseDTO getAttendanceById(Long id) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + id));
        return mapToDTO(attendance);
    }

    public AttendanceResponseDTO recordAttendance(AttendanceRequestDTO dto) {
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        User student = userRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Attendance attendance = Attendance.builder()
                .course(course)
                .student(student)
                .date(dto.getDate())
                .status(dto.getStatus())
                .build();

        Attendance record = attendanceRepo.save(attendance);

        return mapToDTO(record);

    }

    public AttendanceResponseDTO updateAttendance(Long id, AttendanceRequestDTO dto) {

       Attendance attendance = attendanceRepo.findById(id)
               .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + id));

       attendance.setStatus(dto.getStatus());
       if (dto.getCourseId() != null) {
           Course course = courseRepo.findById(dto.getCourseId())
                   .orElseThrow(() -> new EntityNotFoundException("Course not found"));
           attendance.setCourse(course);
       }
       if (dto.getStudentId() != null) {
           User student = userRepo.findById(dto.getStudentId())
                   .orElseThrow(() -> new EntityNotFoundException("Student not found"));
           attendance.setStudent(student);
       }
       if (dto.getDate() != null) {
           attendance.setDate(dto.getDate());
       }

       Attendance saved = attendanceRepo.save(attendance);

       return mapToDTO(saved);

    }

    public void deleteAttendance(Long id) {
        Attendance attendance = attendanceRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + id));
        attendanceRepo.delete(attendance);
    }

    public AttendanceResponseDTO mapToDTO(Attendance dto) {
        return AttendanceResponseDTO.builder()
                .id(dto.getId())
                .courseId(dto.getCourse().getId())
                .courseName(dto.getCourse().getName())
                .studentId(dto.getStudent().getId())
                .studentName(dto.getStudent().toString())
                .date(dto.getDate())
                .status(dto.getStatus())
                .build();
    }
}
