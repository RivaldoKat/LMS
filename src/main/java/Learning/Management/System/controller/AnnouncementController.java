package Learning.Management.System.controller;

import Learning.Management.System.dto.AnnouncementRequestDTO;
import Learning.Management.System.dto.AnnouncementResponseDTO;
import Learning.Management.System.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponseDTO> getAnnouncementById(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AnnouncementResponseDTO> createAnnouncement(@Valid @RequestBody AnnouncementRequestDTO dto) {
        return ResponseEntity.ok(announcementService.createAnnouncement(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponseDTO> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementRequestDTO dto) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}
