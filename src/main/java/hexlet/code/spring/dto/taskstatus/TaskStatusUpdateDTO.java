package hexlet.code.spring.dto.taskstatus;

import jakarta.validation.constraints.NotEmpty;
import org.openapitools.jackson.nullable.JsonNullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    @NotEmpty
    private JsonNullable<String> name;
    @NotEmpty
    private JsonNullable<String> slug;
}
