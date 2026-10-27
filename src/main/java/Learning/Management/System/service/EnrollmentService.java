package Learning.Management.System.service;

import Learning.Management.System.dto.EnrollmentRequestDTO;
import Learning.Management.System.dto.EnrollmentResponseDTO;
import Learning.Management.System.model.Course;
import Learning.Management.System.model.Enrollment;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.EnrollmentRepo;
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
public class EnrollmentService {
    
    private final EnrollmentRepo enrollmentRepo;
    private final CourseRepo courseRepo;
    private final UserRepo userRepo;
    
    public List<EnrollmentResponseDTO> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepo.findByCourseId(courseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EnrollmentResponseDTO getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));
        return mapToDTO(enrollment);
    }

    public EnrollmentResponseDTO enrollStudent(EnrollmentRequestDTO dto){
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        User student = userRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Enrollment enrolled = Enrollment.builder()
                .course(course)
                .student(student)
                .build();

        Enrollment saved = enrollmentRepo.save(enrolled);

        return mapToDTO(saved);
    }

    public EnrollmentResponseDTO updateEnrollment(Long id, EnrollmentRequestDTO dto) {
        Enrollment enrollment = enrollmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));

        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        User student = userRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        enrollment.setCourse(course);
        enrollment.setStudent(student);

        return mapToDTO(enrollmentRepo.save(enrollment));
    }

    public void deleteEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));
        enrollmentRepo.delete(enrollment);
    }

    private EnrollmentResponseDTO mapToDTO(Enrollment enrollment) {
        return EnrollmentResponseDTO.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .studentId(enrollment.getStudent().getId())
                .courseName(enrollment.getCourse().getName())
                .studentName(enrollment.getStudent().toString())
                .enrollmentAt(enrollment.getEnrolledAt())
                .build();
    }
}
