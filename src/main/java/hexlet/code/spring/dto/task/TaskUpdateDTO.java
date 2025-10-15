package hexlet.code.spring.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    @NotEmpty
    private JsonNullable<String> title;
    private JsonNullable<Integer> index;
    private JsonNullable<String> content;
    @NotNull
    private JsonNullable<String> status;
    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;
    private JsonNullable<Set<Long>> taskLabelIds;
}
