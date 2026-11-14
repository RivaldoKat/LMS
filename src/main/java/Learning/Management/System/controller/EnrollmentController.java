package Learning.Management.System.controller;

import Learning.Management.System.dto.EnrollmentRequestDTO;
import Learning.Management.System.dto.EnrollmentResponseDTO;
import Learning.Management.System.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/enrollments")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/course/{courseId}")
    public Page<EnrollmentResponseDTO> getEnrollmentByCourse(
            @Valid @PathVariable Long courseId, @PageableDefault(sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return enrollmentService.getEnrollmentsByCourse(courseId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDTO> getEnrollmentById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping
    public ResponseEntity<EnrollmentResponseDTO> enrollStudent(@Valid @RequestBody EnrollmentRequestDTO dto) {
        return ResponseEntity.ok(enrollmentService.enrollStudent(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDTO> updateEnrollment(@PathVariable Long id, @Valid @RequestBody EnrollmentRequestDTO dto) {
        return ResponseEntity.ok(enrollmentService.updateEnrollment(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
