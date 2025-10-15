package hexlet.code.spring.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    private Integer index;
    @JsonProperty("assignee_id")
    private Long assigneeId;
    @NotEmpty
    private String title;
    private String content;
    @NotNull
    private String status;
    private Set<Long> taskLabelIds;
}
