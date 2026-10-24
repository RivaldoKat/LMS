package Learning.Management.System.controller;

import Learning.Management.System.dto.AnnouncementRequestDTO;
import Learning.Management.System.dto.AnnouncementResponseDTO;
import Learning.Management.System.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/announcements/")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponseDTO>> getAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncement());
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponseDTO> createAnnouncement(@Valid @RequestBody AnnouncementRequestDTO dto) {
        return ResponseEntity.ok(announcementService.createAnnouncement(dto));
    }
}
