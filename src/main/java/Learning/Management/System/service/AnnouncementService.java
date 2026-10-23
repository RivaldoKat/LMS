package Learning.Management.System.service;

import Learning.Management.System.dto.AnnouncementRequestDTO;
import Learning.Management.System.dto.AnnouncementResponseDTO;
import Learning.Management.System.model.Announcement;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.AnnouncementRepo;
import Learning.Management.System.repository.UserRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepo announcementRepo;
    private final UserRepo userRepo;

    public List<AnnouncementResponseDTO> getAllAnnouncement() {
        return announcementRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AnnouncementResponseDTO createAnnouncement(AnnouncementRequestDTO dto) {
        User author = userRepo.findById(dto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Announcement announcement = Announcement.builder()
                .author(author)
                .content(dto.getContent())
                .title(dto.getTitle())
                .build();

        Announcement saved = announcementRepo.save(announcement);

        return mapToDTO(saved);
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
