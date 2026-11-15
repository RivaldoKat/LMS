package Learning.Management.System.service;

import Learning.Management.System.dto.CourseResourceResponseDTO;
import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.CourseResource;
import Learning.Management.System.entity.Role;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.CourseResourceRepo;
import Learning.Management.System.repository.EnrollmentRepo;
import Learning.Management.System.repository.StorageRepo;
import Learning.Management.System.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseResourceService {

    private final CourseResourceRepo resourceRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;
    private final StorageRepo storageRepo;

    public List<CourseResourceResponseDTO> getResources(Long courseId, Authentication authentication) {
        Course course = findCourse(courseId);
        requireCourseAccess(course, getRequester(authentication));
        return resourceRepo.findByCourseIdOrderByUploadedAtDesc(courseId).stream()
                .map(this::mapResource)
                .toList();
    }

    public CourseResourceResponseDTO uploadResource(
            Long courseId, String title, MultipartFile file, Authentication authentication) {
        Course course = findCourse(courseId);
        User requester = getRequester(authentication);
        if (requester.getRole() != Role.TEACHER
                || course.getTeacher() == null
                || !course.getTeacher().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You can only upload notes to your own courses");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Enter a title for these notes");
        }
        String originalFilename = file.getOriginalFilename() == null
                ? "course-notes"
                : StringUtils.getFilename(file.getOriginalFilename().replace('\\', '/'));
        if (originalFilename == null || originalFilename.isBlank() || originalFilename.length() > 255) {
            throw new IllegalArgumentException("The notes filename is invalid or too long");
        }
        String storedFilename = storageRepo.store(file);
        CourseResource resource = CourseResource.builder()
                .course(course)
                .uploadedBy(requester)
                .title(title.trim())
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .contentType("application/octet-stream")
                .build();
        return mapResource(resourceRepo.save(resource));
    }

    public void deleteResource(Long resourceId, Authentication authentication) {
        CourseResource resource = findResource(resourceId);
        User requester = getRequester(authentication);
        if (requester.getRole() != Role.ADMIN
                && (requester.getRole() != Role.TEACHER
                || resource.getCourse().getTeacher() == null
                || !resource.getCourse().getTeacher().getId().equals(requester.getId()))) {
            throw new AccessDeniedException("You can only remove notes from your own courses");
        }
        resourceRepo.delete(resource);
        resourceRepo.flush();
        storageRepo.deleteFile(resource.getStoredFilename());
    }

    public DownloadedResource downloadResource(Long resourceId, Authentication authentication) {
        CourseResource resource = findResource(resourceId);
        requireCourseAccess(resource.getCourse(), getRequester(authentication));
        return new DownloadedResource(
                storageRepo.loadAsResource(resource.getStoredFilename()),
                resource.getOriginalFilename(),
                resource.getContentType());
    }

    private Course findCourse(Long courseId) {
        return courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    private CourseResource findResource(Long resourceId) {
        return resourceRepo.findById(resourceId)
                .orElseThrow(() -> new EntityNotFoundException("Course notes not found"));
    }

    private User getRequester(Authentication authentication) {
        return userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
    }

    private void requireCourseAccess(Course course, User requester) {
        if (requester.getRole() == Role.ADMIN) return;
        if (requester.getRole() == Role.TEACHER
                && course.getTeacher() != null
                && course.getTeacher().getId().equals(requester.getId())) return;
        if (requester.getRole() == Role.LEARNER
                && enrollmentRepo.existsByCourseIdAndStudentId(course.getId(), requester.getId())) return;
        throw new AccessDeniedException("You are not allowed to access notes for this course");
    }

    private CourseResourceResponseDTO mapResource(CourseResource resource) {
        return CourseResourceResponseDTO.builder()
                .id(resource.getId())
                .courseId(resource.getCourse().getId())
                .courseName(resource.getCourse().getName())
                .title(resource.getTitle())
                .originalFilename(resource.getOriginalFilename())
                .contentType(resource.getContentType())
                .uploadedAt(resource.getUploadedAt())
                .downloadUrl("/api/course-resources/" + resource.getId() + "/file")
                .build();
    }

    public record DownloadedResource(Resource resource, String originalFilename, String contentType) { }
}
