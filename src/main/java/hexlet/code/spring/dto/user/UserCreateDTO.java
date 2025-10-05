package hexlet.code.spring.dto.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class UserCreateDTO {
    private final int minLengthPassword = 3;

    private String firstName;
    private String lastName;
    @Email
    private String email;
    @NotBlank
    @Size(min = minLengthPassword, message = "Пароль должен быть не короче трех символов.")
    private String password;
}
