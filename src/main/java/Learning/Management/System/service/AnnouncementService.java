package Learning.Management.System.service;

import Learning.Management.System.dto.AnnouncementRequestDTO;
import Learning.Management.System.dto.AnnouncementResponseDTO;
import Learning.Management.System.entity.Announcement;
import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.AnnouncementRepo;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.EnrollmentRepo;
import Learning.Management.System.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AnnouncementRepo announcementRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;

    public Page<AnnouncementResponseDTO> getAllAnnouncement(Pageable pageable, Authentication authentication) {
        Page<Announcement> announcements = isLearner(authentication)
                ? announcementRepo.findVisibleToLearner(getLearnerId(authentication), pageable)
                : announcementRepo.findAll(pageable);
        return announcements
                .map(this::mapToDTO);
    }

    public AnnouncementResponseDTO getAnnouncementById(Long id, Authentication authentication) {
        Announcement announcement = announcementRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));
        if (isLearner(authentication) && !isVisibleToLearner(announcement, getLearnerId(authentication))) {
            throw new AccessDeniedException("This announcement is not available to you");
        }
        return mapToDTO(announcement);
    }

    public AnnouncementResponseDTO createAnnouncement(AnnouncementRequestDTO dto, Authentication authentication) {
        User author = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
        Course course = dto.getCourseId() == null ? null : courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Announcement announcement = Announcement.builder()
                .author(author)
                .content(dto.getContent())
                .title(dto.getTitle())
                .course(course)
                .build();

        Announcement saved = announcementRepo.save(announcement);

        return mapToDTO(saved);
    }

    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));
        announcementRepo.delete(announcement);
    }

    public AnnouncementResponseDTO updateAnnouncement(
            Long id, AnnouncementRequestDTO dto) {

        Announcement announcementId = announcementRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));

        Course course = dto.getCourseId() == null ? null : courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        announcementId.setTitle(dto.getTitle());
        announcementId.setContent(dto.getContent());
        announcementId.setCourse(course);

        announcementRepo.save(announcementId);

        return mapToDTO(announcementId);

    }

    public AnnouncementResponseDTO mapToDTO(Announcement dto) {
        return AnnouncementResponseDTO.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .authorName(dto.getAuthor().getFirstName() + " " + dto.getAuthor().getLastName())
                .content(dto.getContent())
                .courseId(dto.getCourse() == null ? null : dto.getCourse().getId())
                .courseName(dto.getCourse() == null ? null : dto.getCourse().getName())
                .createdAt(dto.getCreatedAt())
                .build();
   }

    private boolean isVisibleToLearner(Announcement announcement, Long learnerId) {
        return announcement.getCourse() == null
                || enrollmentRepo.existsByCourseIdAndStudentId(announcement.getCourse().getId(), learnerId);
    }

    private boolean isLearner(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_LEARNER"));
    }

    private Long getLearnerId(Authentication authentication) {
        return userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"))
                .getId();
    }
}
