package Learning.Management.System.controller;

import Learning.Management.System.dto.QuizAttemptRequestDTO;
import Learning.Management.System.dto.QuizAttemptResponseDTO;
import Learning.Management.System.dto.QuizGradeRequestDTO;
import Learning.Management.System.dto.QuizCourseOptionDTO;
import Learning.Management.System.dto.QuizRequestDTO;
import Learning.Management.System.dto.QuizResponseDTO;
import Learning.Management.System.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/courses")
    public List<QuizCourseOptionDTO> getTeacherCourses(Authentication authentication) {
        return quizService.getTeacherCourses(authentication);
    }

    @GetMapping
    public List<QuizResponseDTO> getQuizzes(Authentication authentication) {
        return quizService.getQuizzes(authentication);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponseDTO> getQuiz(
            @PathVariable Long quizId, Authentication authentication) {
        return ResponseEntity.ok(quizService.getQuiz(quizId, authentication));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ResponseEntity<QuizResponseDTO> createQuiz(
            @Valid @RequestBody QuizRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(quizService.createQuiz(dto, authentication));
    }

    @PreAuthorize("hasRole('LEARNER')")
    @PostMapping("/{quizId}/attempts")
    public ResponseEntity<QuizAttemptResponseDTO> submitAttempt(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizAttemptRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(quizService.submitAttempt(quizId, dto, authentication));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @GetMapping("/{quizId}/attempts")
    public List<QuizAttemptResponseDTO> getAttempts(
            @PathVariable Long quizId, Authentication authentication) {
        return quizService.getAttempts(quizId, authentication);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PutMapping("/attempts/{attemptId}/grade")
    public ResponseEntity<QuizAttemptResponseDTO> gradeAttempt(
            @PathVariable Long attemptId,
            @Valid @RequestBody QuizGradeRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(quizService.gradeAttempt(attemptId, dto, authentication));
    }
}
