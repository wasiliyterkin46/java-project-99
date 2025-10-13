package hexlet.code.spring.dto.label;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelUpdateDTO {
    @SuppressWarnings("checkstyle:magicnumber")
    @Size(min = 3, max = 1000) //NOSONAR
    @NotEmpty
    private String name;
}
