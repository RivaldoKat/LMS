package Learning.Management.System.controller;

import Learning.Management.System.dto.AssignmentRequestDTO;
import Learning.Management.System.dto.AssignmentResponseDTO;
import Learning.Management.System.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    public Page<AssignmentResponseDTO> getAllAssignments(
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable page,
            Authentication authentication) {
        return assignmentService.getAllAssignments(page, authentication);
    }

    @GetMapping("course/{courseId}")
    public Page<AssignmentResponseDTO> getAssignmentsByCourse(
            @Valid @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable page,
            Authentication authentication) {
        return assignmentService.getAssignmentsByCourse(courseId, page, authentication);
    }

    @GetMapping("/count")
    public long countAssignments(Authentication authentication) {
        return assignmentService.countAssignments(authentication);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDTO> getAssignmentById(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping
    public ResponseEntity<AssignmentResponseDTO> createAssignment(@Valid @RequestBody AssignmentRequestDTO dto) {
        return ResponseEntity.ok(assignmentService.createAssignment(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequestDTO dto) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
