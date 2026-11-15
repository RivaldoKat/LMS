package Learning.Management.System.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class QuizRequestDTO {

    @NotNull
    private Long courseId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private OffsetDateTime opensAt;

    @NotNull
    private OffsetDateTime closesAt;

    @NotEmpty
    private List<@Valid QuizQuestionRequestDTO> questions;
}
