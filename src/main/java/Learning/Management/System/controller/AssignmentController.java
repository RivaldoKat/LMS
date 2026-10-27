package Learning.Management.System.controller;

import Learning.Management.System.dto.AssignmentRequestDTO;
import Learning.Management.System.dto.AssignmentResponseDTO;
import Learning.Management.System.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("course/{courseId}")
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignmentsByCourse(@Valid @PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDTO> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AssignmentResponseDTO> createAssignment(@Valid @RequestBody AssignmentRequestDTO dto) {
        return ResponseEntity.ok(assignmentService.createAssignment(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequestDTO dto) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
