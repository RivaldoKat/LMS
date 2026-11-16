package Learning.Management.System.service;

import Learning.Management.System.dto.AssignmentRequestDTO;
import Learning.Management.System.dto.AssignmentResponseDTO;
import Learning.Management.System.entity.Assignment;
import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.Role;
import Learning.Management.System.entity.User;
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

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepo assignmentRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;

    public Page<AssignmentResponseDTO> getAllAssignments(Pageable page, Authentication authentication) {
        User requester = getRequester(authentication);
        Page<Assignment> assignments = switch (requester.getRole()) {
            case ADMIN -> assignmentRepo.findAll(page);
            case TEACHER -> assignmentRepo.findByCourseTeacherId(requester.getId(), page);
            case LEARNER -> assignmentRepo.findVisibleToLearner(requester.getId(), page);
            default -> throw new AccessDeniedException("You are not allowed to view assignments");
        };
        return assignments.map(this::mapToDTO);
    }

    public Page<AssignmentResponseDTO> getAssignmentsByCourse(
            Long courseId, Pageable page, Authentication authentication) {
        requireCourseAccess(courseId, authentication);
        return assignmentRepo.findByCourseId(courseId, page)
                .map(this::mapToDTO);
    }

    public AssignmentResponseDTO getAssignmentById(Long id, Authentication authentication) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        requireCourseAccess(assignment.getCourse().getId(), authentication);
        return mapToDTO(assignment);
    }

    public long countAssignments(Authentication authentication) {
        User requester = getRequester(authentication);
        return switch (requester.getRole()) {
            case ADMIN -> assignmentRepo.count();
            case TEACHER -> assignmentRepo.countByCourseTeacherId(requester.getId());
            case LEARNER -> assignmentRepo.countVisibleToLearner(requester.getId());
            default -> throw new AccessDeniedException("You are not allowed to view assignments");
        };
    }

    public AssignmentResponseDTO createAssignment(AssignmentRequestDTO dto, Authentication authentication) {
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        requireCourseManagementAccess(course, authentication);

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

    public AssignmentResponseDTO updateAssignment(
            Long id, AssignmentRequestDTO dto, Authentication authentication) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        requireCourseManagementAccess(assignment.getCourse(), authentication);

        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        requireCourseManagementAccess(course, authentication);

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

    public void deleteAssignment(Long id, Authentication authentication) {
        Assignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
        requireCourseManagementAccess(assignment.getCourse(), authentication);
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

    private void requireCourseAccess(Long courseId, Authentication authentication) {
        User requester = getRequester(authentication);
        if (requester.getRole() == Role.ADMIN) return;
        if (requester.getRole() == Role.TEACHER) {
            Course course = courseRepo.findById(courseId)
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));
            if (course.getTeacher() != null && course.getTeacher().getId().equals(requester.getId())) return;
            throw new AccessDeniedException("You can only view assignments for your own courses");
        }
        if (requester.getRole() == Role.LEARNER
                && enrollmentRepo.existsByCourseIdAndStudentId(courseId, requester.getId())) return;
        throw new AccessDeniedException("You are not allowed to view assignments for this course");
    }

    private void requireCourseManagementAccess(Course course, Authentication authentication) {
        User requester = getRequester(authentication);
        if (requester.getRole() == Role.ADMIN) return;
        if (requester.getRole() != Role.TEACHER
                || course.getTeacher() == null
                || !course.getTeacher().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You can only manage assignments for your own courses");
        }
    }

    private User getRequester(Authentication authentication) {
        return userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
    }
}
