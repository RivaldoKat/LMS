package Learning.Management.System.service;

import Learning.Management.System.dto.SubmissionRequestDTO;
import Learning.Management.System.dto.SubmissionResponseDTO;
import Learning.Management.System.model.Assignment;
import Learning.Management.System.model.Submission;
import Learning.Management.System.model.SubmissionStatus;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.AssignmentRepo;
import Learning.Management.System.repository.SubmissionRepo;
import Learning.Management.System.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepo submissionRepo;
    private final AssignmentRepo assignmentRepo;
    private final UserRepo userRepo;

    public List<SubmissionResponseDTO> getSubmissionByStudent(Long studentId) {
        return submissionRepo.findStudentById(studentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public SubmissionResponseDTO submitAssignment(SubmissionRequestDTO dto) {
        Assignment assignment = assignmentRepo.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        User student = userRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

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
                .assignmentId(submission.getId())
                .studentId(submission.getStudent().getId())
                .fileUrl(submission.getFileUrl())
                .studentName(submission.getStudent().toString())
                .submittedAt(submission.getSubmittedAt())
                .grade(submission.getGrade())
                .feedback(submission.getFeedback())
                .status(submission.getStatus())
                .build();
    }

}
