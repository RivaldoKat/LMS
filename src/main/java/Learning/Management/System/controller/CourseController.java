package Learning.Management.System.controller;


import Learning.Management.System.dto.CourseResponseDTO;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses() {
        return
                ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getCourseById(@Valid @PathVariable Long id) {
        CourseResponseDTO course = courseService.getCourseById(id);
        if(course == null) {
            return
                    ResponseEntity.notFound().build();
        }
        return
                ResponseEntity.ok(course);
    }
}
