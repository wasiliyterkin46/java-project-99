package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.user.UserCreateDTO;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.dto.user.UserUpdateDTO;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import hexlet.code.spring.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(uses = { JsonNullableMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMainMapper {
    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @BeforeMapping
    public final void encryptPassword(final UserCreateDTO data) {
        var password = data.getPassword();
        data.setPassword(encoder.encode(password));
    }

    @BeforeMapping
    public final void encryptPassword(final UserUpdateDTO dto, @MappingTarget final User model) {
        if (jsonNullableMapper.isPresent(dto.getPassword())) {
            model.setPasswordDigest(encoder.encode(jsonNullableMapper.unwrap(dto.getPassword())));
        }
    }

    // Добавить процедуру кодирования пароля для операции обновления

    @Mapping(target = "passwordDigest", source = "password")
    public abstract User map(UserCreateDTO dto);

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract UserDTO map(User model);

    @Mapping(target = "createdAt", ignore = true)
    public abstract User map(UserDTO dto);

    @Mapping(target = "passwordDigest", ignore = true)
    public abstract void update(UserUpdateDTO dto, @MappingTarget User model);
}
