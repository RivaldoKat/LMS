package Learning.Management.System.service;

import Learning.Management.System.dto.AnnouncementRequestDTO;
import Learning.Management.System.dto.AnnouncementResponseDTO;
import Learning.Management.System.model.Announcement;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.AnnouncementRepo;
import Learning.Management.System.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AnnouncementRepo announcementRepo;
    private final UserRepo userRepo;

    public List<AnnouncementResponseDTO> getAllAnnouncement() {
        return announcementRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AnnouncementResponseDTO getAnnouncementById(Long id) {
        Announcement announcement = announcementRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));
        return mapToDTO(announcement);
    }

    public AnnouncementResponseDTO createAnnouncement(AnnouncementRequestDTO dto) {
        User author = userRepo.findById(dto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        Announcement announcement = Announcement.builder()
                .author(author)
                .content(dto.getContent())
                .title(dto.getTitle())
                .build();

        Announcement saved = announcementRepo.save(announcement);

        return mapToDTO(saved);
    }

    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));
        announcementRepo.delete(announcement);
    }

    public AnnouncementResponseDTO updateAnnouncement(Long id, AnnouncementRequestDTO dto) {

        Announcement announcementId = announcementRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));


        announcementId.setTitle(dto.getTitle());
        announcementId.setContent(dto.getContent());

        announcementRepo.save(announcementId);

        return mapToDTO(announcementId);

    }

    public AnnouncementResponseDTO mapToDTO(Announcement dto) {
        return AnnouncementResponseDTO.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .authorName(dto.getAuthor().toString())
                .content(dto.getContent())
                .createdAt(dto.getCreatedAt())
                .build();
   }
}
