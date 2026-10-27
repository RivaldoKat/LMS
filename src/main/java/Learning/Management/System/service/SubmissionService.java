package Learning.Management.System.service;

import Learning.Management.System.dto.GradeRequestDTO;
import Learning.Management.System.dto.SubmissionRequestDTO;
import Learning.Management.System.dto.SubmissionResponseDTO;
import Learning.Management.System.model.Assignment;
import Learning.Management.System.model.Submission;
import Learning.Management.System.model.SubmissionStatus;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.AssignmentRepo;
import Learning.Management.System.repository.SubmissionRepo;
import Learning.Management.System.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private final SubmissionRepo submissionRepo;
    private final AssignmentRepo assignmentRepo;
    private final UserRepo userRepo;

    public List<SubmissionResponseDTO> getSubmissionByStudent(Long studentId, Authentication authentication) {
        User requester = getRequester(authentication);
        if (isAdmin(authentication)) {
            return submissionRepo.findByStudentId(studentId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }
        if (!isTeacher(authentication) && !requester.getId().equals(studentId)) {
            throw new AccessDeniedException("You can only view your own submissions");
        }
        return submissionRepo.findByStudentId(studentId).stream()
                .filter(submission -> !isTeacher(authentication)
                        || isCourseTeacher(submission, requester))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public SubmissionResponseDTO getSubmissionById(Long id, Authentication authentication) {
        Submission submission = submissionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + id));
        requireOwnerOrStaff(submission, authentication);
        return mapToDTO(submission);
    }

    public SubmissionResponseDTO submitAssignment(SubmissionRequestDTO dto, Authentication authentication) {
        Assignment assignment = assignmentRepo.findById(dto.getAssignmentId())
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        User student = getRequester(authentication);

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .fileUrl(dto.getFileUrl())
                .status(SubmissionStatus.SUBMITTED)
                .build();

        Submission saved = submissionRepo.save(submission);

        return
                mapToDTO(saved);


    }

    private SubmissionResponseDTO mapToDTO(Submission submission) {
        return SubmissionResponseDTO.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .studentId(submission.getStudent().getId())
                .fileUrl(submission.getFileUrl())
                .studentName(submission.getStudent().toString())
                .submittedAt(submission.getSubmittedAt())
                .grade(submission.getGrade())
                .feedback(submission.getFeedback())
                .status(submission.getStatus())
                .build();
    }

    public SubmissionResponseDTO updateSubmission(
            Long submissionId, SubmissionRequestDTO dto, Authentication authentication) {
        Submission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
        requireOwner(submission, authentication);

        Assignment assignment = assignmentRepo.findById(dto.getAssignmentId())
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!submission.getAssignment().getId().equals(assignment.getId())) {
            throw new IllegalArgumentException("A submission cannot be moved to another assignment");
        }
        submission.setFileUrl(dto.getFileUrl());

        return mapToDTO(submissionRepo.save(submission));
    }

    public void deleteSubmission(Long submissionId, Authentication authentication) {
        Submission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
        requireOwner(submission, authentication);
        submissionRepo.delete(submission);
    }

    public SubmissionResponseDTO gradeSubmission(
            Long submissionId, GradeRequestDTO dto, Authentication authentication) {
        Submission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        User teacher = getRequester(authentication);
        if (submission.getAssignment().getCourse().getTeacher() == null
                || !submission.getAssignment().getCourse().getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("You can only grade submissions for your own courses");
        }

        submission.setGrade(dto.getGrade());
        submission.setFeedback(dto.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);

        Submission updated = submissionRepo.save(submission);
        return
                mapToDTO(updated);
    }

    private User getRequester(Authentication authentication) {
        return userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
    }

    private void requireOwnerOrStaff(Submission submission, Authentication authentication) {
        User requester = getRequester(authentication);
        if (isAdmin(authentication) || isCourseTeacher(submission, requester)) {
            return;
        }
        if (!requester.getId().equals(submission.getStudent().getId())) {
            throw new AccessDeniedException("You can only view your own submissions or submissions for your courses");
        }
    }

    private void requireOwner(Submission submission, Authentication authentication) {
        User requester = getRequester(authentication);
        if (!requester.getId().equals(submission.getStudent().getId())) {
            throw new AccessDeniedException("You can only manage your own submissions");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isTeacher(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TEACHER"));
    }

    private boolean isCourseTeacher(Submission submission, User requester) {
        return isTeacherUser(requester)
                && submission.getAssignment().getCourse().getTeacher() != null
                && submission.getAssignment().getCourse().getTeacher().getId().equals(requester.getId());
    }

    private boolean isTeacherUser(User user) {
        return user.getRole() == Learning.Management.System.model.Role.TEACHER;
    }
}
