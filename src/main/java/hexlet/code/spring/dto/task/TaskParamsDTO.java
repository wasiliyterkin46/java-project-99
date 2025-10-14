package hexlet.code.spring.dto.task;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskParamsDTO {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;
    private String sortField;
    private String sortOrder;
    private Long start;
    private Long end;
}
