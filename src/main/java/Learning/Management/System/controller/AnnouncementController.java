package Learning.Management.System.controller;

import Learning.Management.System.dto.AnnouncementRequestDTO;
import Learning.Management.System.dto.AnnouncementResponseDTO;
import Learning.Management.System.service.AnnouncementService;
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
@RequestMapping("api/announcements/")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Page<AnnouncementResponseDTO> getAnnouncements(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        return announcementService.getAllAnnouncement(pageable, authentication);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponseDTO> getAnnouncementById(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping
    public ResponseEntity<AnnouncementResponseDTO> createAnnouncement(
            @Valid @RequestBody AnnouncementRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(announcementService.createAnnouncement(dto, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponseDTO> updateAnnouncement(
            @PathVariable Long id, @Valid @RequestBody AnnouncementRequestDTO dto) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}
