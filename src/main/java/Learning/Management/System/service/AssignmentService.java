package Learning.Management.System.service;

import Learning.Management.System.dto.AssignmentRequestDTO;
import Learning.Management.System.dto.AssignmentResponseDTO;
import Learning.Management.System.entity.Assignment;
import Learning.Management.System.entity.Course;
import Learning.Management.System.repository.AssignmentRepo;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepo assignmentRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;

    public Page<AssignmentResponseDTO> getAllAssignments(Pageable page, Authentication authentication) {
        Page<Assignment> assignments = isLearner(authentication)
                ? assignmentRepo.findVisibleToLearner(getLearnerId(authentication), page)
                : assignmentRepo.findAll(page);
        return assignments.map(this::mapToDTO);
    }

    public Page<AssignmentResponseDTO> getAssignmentsByCourse(
            Long courseId, Pageable page, Authentication authentication) {
        requireEnrolledLearner(courseId, authentication);
        return assignmentRepo.findByCourseId(courseId, page)
                .map(this::mapToDTO);
    }

    public AssignmentResponseDTO getAssignmentById(Long id, Authentication authentication) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        requireEnrolledLearner(assignment.getCourse().getId(), authentication);
        return mapToDTO(assignment);
    }

    public long countAssignments(Authentication authentication) {
        if (!isLearner(authentication)) {
            return assignmentRepo.count();
        }
        return assignmentRepo.countVisibleToLearner(getLearnerId(authentication));
    }

    public AssignmentResponseDTO createAssignment(AssignmentRequestDTO dto) {
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .attachmentUrl(dto.getAttachmentUrl())
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
        if (dto.getAttachmentUrl() != null) {
            assignment.setAttachmentUrl(dto.getAttachmentUrl());
        }
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
                .attachmentUrl(assignment.getAttachmentUrl())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .build();
    }

    private void requireEnrolledLearner(Long courseId, Authentication authentication) {
        if (isLearner(authentication)
                && !enrollmentRepo.existsByCourseIdAndStudentId(courseId, getLearnerId(authentication))) {
            throw new AccessDeniedException("You can only view assignments for your enrolled courses");
        }
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
