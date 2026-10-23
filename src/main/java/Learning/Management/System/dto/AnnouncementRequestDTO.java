package Learning.Management.System.dto;

import lombok.Data;

@Data
public class AnnouncementRequestDTO {
    private String title;
    private String content;
    private Long authorId;
}
