package Learning.Management.System.controller;

import Learning.Management.System.dto.QuizImageResponseDTO;
import Learning.Management.System.repository.StorageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/quizzes/images")
@RequiredArgsConstructor
public class QuizImageController {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/png", "image/jpeg", "image/gif", "image/webp");
    private final StorageRepo storageRepo;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public QuizImageResponseDTO uploadImage(@RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType() == null
                ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String filename = file.getOriginalFilename() == null
                ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        boolean extensionMatches = switch (contentType) {
            case "image/png" -> filename.endsWith(".png");
            case "image/jpeg" -> filename.endsWith(".jpg") || filename.endsWith(".jpeg");
            case "image/gif" -> filename.endsWith(".gif");
            case "image/webp" -> filename.endsWith(".webp");
            default -> false;
        };
        if (file.isEmpty() || file.getSize() > MAX_IMAGE_SIZE
                || !ALLOWED_TYPES.contains(contentType) || !extensionMatches) {
            throw new IllegalArgumentException("Choose a PNG, JPEG, GIF, or WEBP image under 10 MB");
        }
        String storedFilename = storageRepo.store(file);
        return QuizImageResponseDTO.builder()
                .imageUrl("/api/quizzes/images/" + storedFilename)
                .build();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource image = storageRepo.loadAsResource(filename);
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        MediaType contentType = switch (extension) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(image.getFilename(), StandardCharsets.UTF_8)
                        .build().toString())
                .body(image);
    }
}
