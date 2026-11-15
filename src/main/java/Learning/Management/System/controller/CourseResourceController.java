package Learning.Management.System.controller;

import Learning.Management.System.dto.CourseResourceResponseDTO;
import Learning.Management.System.service.CourseResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CourseResourceController {

    private static final long MAX_FILE_SIZE = 25L * 1024 * 1024;
    private final CourseResourceService resourceService;

    @GetMapping("/api/courses/{courseId}/resources")
    public List<CourseResourceResponseDTO> getResources(
            @PathVariable Long courseId, Authentication authentication) {
        return resourceService.getResources(courseId, authentication);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping(value = "/api/courses/{courseId}/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseResourceResponseDTO uploadResource(
            @PathVariable Long courseId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Choose a non-empty notes file under 25 MB");
        }
        return resourceService.uploadResource(courseId, title, file, authentication);
    }

    @DeleteMapping("/api/course-resources/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long resourceId, Authentication authentication) {
        resourceService.deleteResource(resourceId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/course-resources/{resourceId}/file")
    public ResponseEntity<Resource> downloadResource(
            @PathVariable Long resourceId, Authentication authentication) {
        CourseResourceService.DownloadedResource resource =
                resourceService.downloadResource(resourceId, authentication);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(resource.originalFilename(), StandardCharsets.UTF_8)
                        .build().toString())
                .body(resource.resource());
    }
}
