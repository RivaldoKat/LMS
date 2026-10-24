package Learning.Management.System.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnnouncementRequestDTO {

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Content is required")
    private String content;

    @NotNull(message = "Author ID is required")
    private Long authorId;
}
