package Learning.Management.System.controller;

import Learning.Management.System.exceptions.StorageFileNotFoundException;
import Learning.Management.System.repository.StorageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final StorageRepo storageRepo;

    @Autowired
    public FileUploadController(StorageRepo storageRepo) {
        this.storageRepo = storageRepo;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) {
        try (Stream<Path> files = storageRepo.loadAll()) {
            List<String> fileUrls = files.map(
                    path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                            "serveFile", path.getFileName().toString()).build().toUri().toString())
                    .collect(Collectors.toList());
            model.addAttribute("files", fileUrls);
        }

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageRepo.loadAsResource(filename);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
     }

     @PostMapping("/")
    public String handleFileUpload(@RequestParam("file")MultipartFile file, RedirectAttributes redirectAttributes) {
        storageRepo.store(file);
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " +
                file.getOriginalFilename() + "!");

        return "redirect:/";
     }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
