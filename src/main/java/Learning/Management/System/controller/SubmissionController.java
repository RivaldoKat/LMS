package Learning.Management.System.controller;

import Learning.Management.System.dto.GradeRequestDTO;
import Learning.Management.System.dto.SubmissionRequestDTO;
import Learning.Management.System.dto.SubmissionResponseDTO;
import Learning.Management.System.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("student/{studentId}")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByStudent(
            @Valid @PathVariable Long studentId, Authentication authentication) {
        return ResponseEntity.ok(submissionService.getSubmissionByStudent(studentId, authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponseDTO> getSubmissionById(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(submissionService.getSubmissionById(id, authentication));
    }

    @PreAuthorize("hasRole('LEARNER')")
    @PostMapping
    public ResponseEntity<SubmissionResponseDTO> submitAssignment(
            @Valid @RequestBody SubmissionRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(submissionService.submitAssignment(dto, authentication));
    }

    @PreAuthorize("hasRole('LEARNER')")
    @PutMapping("/{id}")
    public ResponseEntity<SubmissionResponseDTO> updateSubmission(
            @PathVariable Long id, @Valid @RequestBody SubmissionRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(submissionService.updateSubmission(id, dto, authentication));
    }

    @PreAuthorize("hasRole('LEARNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id, Authentication authentication) {
        submissionService.deleteSubmission(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("{id}/grade")
    public ResponseEntity<SubmissionResponseDTO> gradeSubmission(
            @Valid @PathVariable Long id, @Valid @RequestBody GradeRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(submissionService.gradeSubmission(id, dto, authentication));
    }
}
