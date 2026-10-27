package Learning.Management.System.service;

import Learning.Management.System.dto.AssignmentRequestDTO;
import Learning.Management.System.dto.AssignmentResponseDTO;
import Learning.Management.System.model.Assignment;
import Learning.Management.System.model.Course;
import Learning.Management.System.repository.AssignmentRepo;
import Learning.Management.System.repository.CourseRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepo assignmentRepo;
    private final CourseRepo courseRepo;

    public List<AssignmentResponseDTO> getAssignmentsByCourse(Long courseId) {
        return assignmentRepo.findByCourseId(courseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AssignmentResponseDTO getAssignmentById(Long id) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        return mapToDTO(assignment);
    }

    public AssignmentResponseDTO createAssignment(AssignmentRequestDTO dto) {
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .maxScore(dto.getMaxScore())
                .build();

        Assignment saved = assignmentRepo.save(assignment);

        return mapToDTO(saved);
    }

    public AssignmentResponseDTO updateAssignment(Long id, AssignmentRequestDTO dto) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));

        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        assignment.setCourse(course);
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setMaxScore(dto.getMaxScore());

        return mapToDTO(assignmentRepo.save(assignment));
    }

    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        assignmentRepo.delete(assignment);
    }

    private AssignmentResponseDTO mapToDTO(Assignment assignment) {
        return AssignmentResponseDTO.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourse().getId())
                .courseName(assignment.getCourse().getName())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .build();
    }
}
