package Learning.Management.System.controller;

import Learning.Management.System.dto.AttendanceRequestDTO;
import Learning.Management.System.dto.AttendanceResponseDTO;
import Learning.Management.System.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AttendanceResponseDTO>> getAttendanceByCourse(@Valid @PathVariable Long courseId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<AttendanceResponseDTO> recordAttendance(@Valid @RequestBody AttendanceRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.recordAttendance(dto));
    }
}