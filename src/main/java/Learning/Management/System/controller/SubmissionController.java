package Learning.Management.System.controller;

import Learning.Management.System.dto.GradeRequestDTO;
import Learning.Management.System.dto.SubmissionRequestDTO;
import Learning.Management.System.dto.SubmissionResponseDTO;
import Learning.Management.System.service.FileStorageService;
import Learning.Management.System.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final FileStorageService fileStorageService;

    @GetMapping("student/{studentId}")
    public ResponseEntity<Page<SubmissionResponseDTO>> getSubmissionsByStudent(
            @Valid @PathVariable Long studentId,
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(submissionService.getSubmissionByStudent(studentId, pageable, authentication));
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

    @PreAuthorize("hasAnyRole('LEARNER', 'TEACHER')")
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAssignmentFile(
            @RequestParam("file") MultipartFile file) {
        String storedFilename = fileStorageService.store(file);
        return ResponseEntity.ok(Map.of("fileUrl", "/files/" + storedFilename));
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
