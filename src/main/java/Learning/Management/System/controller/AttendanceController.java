package Learning.Management.System.controller;

import Learning.Management.System.dto.AttendanceRequestDTO;
import Learning.Management.System.dto.AttendanceResponseDTO;
import Learning.Management.System.service.AttendanceService;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/course/{courseId}")
    public Page<AttendanceResponseDTO> getAttendanceByCourse(
            @Valid @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable page) {
        return attendanceService.getAttendanceByCourse(courseId, page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDTO> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping
    public ResponseEntity<AttendanceResponseDTO> recordAttendance(@Valid @RequestBody AttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.recordAttendance(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponseDTO> updateAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}