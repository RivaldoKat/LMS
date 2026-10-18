package Learning.Management.System.dto;

import lombok.Data;

@Data
public class SubmissionRequestDTO {
    private Long assignmentId;
    private Long studentId;
    private String fileUrl;
}
