package Learning.Management.System.controller;

import Learning.Management.System.dto.AssignmentRequestDTO;
import Learning.Management.System.dto.AssignmentResponseDTO;
import Learning.Management.System.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("course/{courseId}")
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignmentsByCourse(@PathVariable Long courseId) {
        return
                ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<AssignmentResponseDTO> createAssignment(@RequestBody AssignmentRequestDTO dto) {
        return
                ResponseEntity.ok(assignmentService.createAssignment(dto));
    }
}
