package Learning.Management.System.service;

import Learning.Management.System.dto.CourseRequestDTO;
import Learning.Management.System.dto.CourseResponseDTO;
import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.EnrollmentRepo;
import Learning.Management.System.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;

    public Page<CourseResponseDTO> getAllCourses(Pageable page, Authentication authentication) {
        Page<Course> courses = isLearner(authentication)
                ? courseRepo.findEnrolledCourses(getLearnerId(authentication), page)
                : courseRepo.findAll(page);
        return courses
                .map(this::mapToDTO);
    }

    public CourseResponseDTO getCourseById(Long id, Authentication authentication) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
        if (isLearner(authentication)
                && !enrollmentRepo.existsByCourseIdAndStudentId(id, getLearnerId(authentication))) {
            throw new AccessDeniedException("You can only view courses you are enrolled in");
        }
        return mapToDTO(course);
    }

    public CourseResponseDTO createCourse(CourseRequestDTO dto) {
        User teacher = userRepo.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + dto.getTeacherId()));

        Course course = Course.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .term(dto.getTerm())
                .schoolTerm(dto.getSchoolTerm())
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
        course.setSchoolTerm(dto.getSchoolTerm());
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
                .schoolTerm(course.getSchoolTerm())
                .teacherName(course.getTeacher() == null ? null
                        : course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName())
                .build();
    }

    private boolean isLearner(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_LEARNER"));
    }

    private Long getLearnerId(Authentication authentication) {
        return userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"))
                .getId();
    }
}
