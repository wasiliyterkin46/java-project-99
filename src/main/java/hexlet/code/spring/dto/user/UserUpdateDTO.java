package hexlet.code.spring.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {
    private final int minLengthPassword = 3;
    @Email
    private JsonNullable<String> email;
    private JsonNullable<String> firstName;
    private JsonNullable<String> lastName;
    @Size(min = minLengthPassword)
    private JsonNullable<String> password;
}
