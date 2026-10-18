package Learning.Management.System.service;


import Learning.Management.System.dto.CourseResponseDTO;
import Learning.Management.System.repository.CourseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {


    private final CourseRepo courseRepo;

    public List<CourseResponseDTO> getAllCourses() {
        return courseRepo.findAll().stream()
                .map(course -> CourseResponseDTO.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .code(course.getCode())
                        .description(course.getDescription())
                        .term(course.getTerm())
                        .teacherName(course.getTeacher().toString())
                        .build())
                .collect(Collectors.toList());
    }


    public CourseResponseDTO getCourseById( Long id) {
        return courseRepo.findById(id)
                .map(course -> CourseResponseDTO.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .code(course.getCode())
                        .description(course.getDescription())
                        .term(course.getTerm())
                        .teacherName(course.getTeacher().toString())
                        .build())
                .orElse(null);
    }


}
