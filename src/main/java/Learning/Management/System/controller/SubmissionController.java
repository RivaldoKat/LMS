package Learning.Management.System.controller;

import Learning.Management.System.dto.GradeRequestDTO;
import Learning.Management.System.dto.SubmissionRequestDTO;
import Learning.Management.System.dto.SubmissionResponseDTO;
import Learning.Management.System.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("student/{studentId}")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByStudent(@Valid @PathVariable Long studentId) {
        return ResponseEntity.ok(submissionService.getSubmissionByStudent(studentId));
    }

    @PostMapping
    public ResponseEntity<SubmissionResponseDTO> submitAssignment(@Valid @RequestBody SubmissionRequestDTO dto) {
        return ResponseEntity.ok(submissionService.submitAssignment(dto));
    }

    @PreAuthorize("hasRole('Teacher')")
    @PutMapping("{id}/grade")
    public ResponseEntity<SubmissionResponseDTO> gradeSubmission(@Valid @PathVariable Long id, @Valid @RequestBody GradeRequestDTO dto) {
        return ResponseEntity.ok(submissionService.gradeSubmission(id, dto));
    }
}
