package Learning.Management.System.controller;

import Learning.Management.System.dto.EnrollmentRequestDTO;
import Learning.Management.System.dto.EnrollmentResponseDTO;
import Learning.Management.System.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollmentByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponseDTO> enrollStudent(@RequestBody EnrollmentRequestDTO dto) {
        return ResponseEntity.ok(enrollmentService.enrollStudent(dto));
    }
}
