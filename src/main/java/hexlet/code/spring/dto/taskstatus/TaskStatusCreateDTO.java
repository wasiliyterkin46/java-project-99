package hexlet.code.spring.dto.taskstatus;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusCreateDTO {
    @NotEmpty
    private String name;
    @NotEmpty
    private String slug;
}
