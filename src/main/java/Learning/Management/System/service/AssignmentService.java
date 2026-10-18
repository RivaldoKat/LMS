package Learning.Management.System.service;

import Learning.Management.System.dto.AssignmentRequestDTO;
import Learning.Management.System.dto.AssignmentResponseDTO;
import Learning.Management.System.model.Assignment;
import Learning.Management.System.model.Course;
import Learning.Management.System.repository.AssignmentRepo;
import Learning.Management.System.repository.CourseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepo assignmentRepo;
    private final CourseRepo courseRepo;

    public List<AssignmentResponseDTO> getAssignmentsByCourse(Long courseId) {
        return assignmentRepo.findByCourseId(courseId).stream()
                .map(a -> AssignmentResponseDTO.builder()
                        .id(a.getId())
                        .courseId(a.getCourse().getId())
                        .courseName(a.getCourse().getName())
                        .title(a.getTitle())
                        .description(a.getDescription())
                        .dueDate(a.getDueDate())
                        .maxScore(a.getMaxScore())
                        .build())
                .collect(Collectors.toList());
    }

    public AssignmentResponseDTO createAssignment(AssignmentRequestDTO dto) {
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .maxScore(dto.getMaxScore())
                .build();

        Assignment saved = assignmentRepo.save(assignment);

        return AssignmentResponseDTO.builder()
                .id(saved.getId())
                .courseId(course.getId())
                .courseName(course.getName())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .dueDate(saved.getDueDate())
                .maxScore(saved.getMaxScore())
                .build();
    }
}
