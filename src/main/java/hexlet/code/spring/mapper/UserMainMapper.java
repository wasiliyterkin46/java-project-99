package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.user.UserCreateDTO;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.dto.user.UserUpdateDTO;
import hexlet.code.spring.exception.RequestDataCannotBeProcessed;
import hexlet.code.spring.repository.UserRepository;
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

    @Autowired
    private UserRepository repository;

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

    @BeforeMapping
    public final void beforeCreate(final UserCreateDTO dto, @MappingTarget final User model) {
        var email = dto.getEmail();
        if (repository.existsByEmail(email)) {
            throw new RequestDataCannotBeProcessed(String.format("Email должен быть уникальным. "
                    + "В базе уже есть пользователь с email = %s", email));
        }
    }

    @BeforeMapping
    public final void beforeUpdate(final UserUpdateDTO dto, @MappingTarget final User model) {
        if (jsonNullableMapper.isPresent(dto.getEmail())) {
            var email = jsonNullableMapper.unwrap(dto.getEmail());
            if (repository.existsByEmail(email)) {
                throw new RequestDataCannotBeProcessed(String.format("Email должен быть уникальным. "
                        + "В базе уже есть пользователь с email = %s", email));
            }
        }
    }

    @Mapping(target = "passwordDigest", source = "password")
    public abstract User mapToModel(UserCreateDTO dto);

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract UserDTO mapToDTO(User model);

    @Mapping(target = "createdAt", ignore = true)
    public abstract User mapToModel(UserDTO dto);

    @Mapping(target = "passwordDigest", ignore = true)
    public abstract void updateModelFromDTO(UserUpdateDTO dto, @MappingTarget User model);
}
