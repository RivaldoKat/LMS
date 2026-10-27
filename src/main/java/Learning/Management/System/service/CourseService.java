package Learning.Management.System.service;

import Learning.Management.System.dto.CourseRequestDTO;
import Learning.Management.System.dto.CourseResponseDTO;
import Learning.Management.System.model.Course;
import Learning.Management.System.model.User;
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
public class CourseService {

    private final CourseRepo courseRepo;
    private final UserRepo userRepo;

    public List<CourseResponseDTO> getAllCourses() {
        return courseRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CourseResponseDTO getCourseById(Long id) {
        return courseRepo.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    public CourseResponseDTO createCourse(CourseRequestDTO dto) {
        User teacher = userRepo.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + dto.getTeacherId()));

        Course course = Course.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .term(dto.getTerm())
                .teacher(teacher)
                .build();

        return mapToDTO(courseRepo.save(course));
    }

    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO dto) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        User teacher = userRepo.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + dto.getTeacherId()));

        course.setName(dto.getName());
        course.setCode(dto.getCode());
        course.setDescription(dto.getDescription());
        course.setTerm(dto.getTerm());
        course.setTeacher(teacher);

        return mapToDTO(courseRepo.save(course));
    }

    public void deleteCourse(Long id) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        courseRepo.delete(course);
    }

    private CourseResponseDTO mapToDTO(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .description(course.getDescription())
                .term(course.getTerm())
                .teacherName(course.getTeacher() != null ? course.getTeacher().toString() : null)
                .build();
    }
}
