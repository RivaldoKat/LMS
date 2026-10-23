package Learning.Management.System.service;

import Learning.Management.System.dto.AttendanceRequestDTO;
import Learning.Management.System.dto.AttendanceResponseDTO;
import Learning.Management.System.model.Attendance;
import Learning.Management.System.model.Course;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.AttendanceRepo;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepo attendanceRepo;
    private final CourseRepo courseRepo;
    private final UserRepo userRepo;

    public List<AttendanceResponseDTO> getAttendanceByCourse(Long courseId) {
        return attendanceRepo.findByCourseId(courseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AttendanceResponseDTO recordAttendance(AttendanceRequestDTO dto) {
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User student = userRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Attendance attendance = Attendance.builder()
                .course(course)
                .student(student)
                .date(dto.getDate())
                .status(dto.getStatus())
                .build();

        Attendance record = attendanceRepo.save(attendance);

        return mapToDTO(record);

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
