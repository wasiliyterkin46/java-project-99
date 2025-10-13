package hexlet.code.spring.dto.label;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelDTO {
    private Long id;
    private String name;
    private String createdAt;
}
