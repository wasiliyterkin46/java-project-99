package hexlet.code.spring.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private String title;
    private Integer index;
    private String content;
    private String status;
    @JsonProperty("assignee_id")
    private Long assigneeId;
    private String createdAt;
    private Set<Long> taskLabelIds;
}
