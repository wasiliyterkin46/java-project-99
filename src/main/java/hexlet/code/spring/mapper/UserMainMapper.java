package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.user.UserCreateDTO;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.dto.user.UserUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import hexlet.code.spring.model.User;

@Mapper(uses = { JsonNullableMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMainMapper {

    @Mapping(target = "passwordDigest", source = "password")
    public abstract User mapToModel(UserCreateDTO dto);

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract UserDTO mapToDTO(User model);

    @Mapping(target = "createdAt", ignore = true)
    public abstract User mapToModel(UserDTO dto);

    @Mapping(target = "passwordDigest", ignore = true)
    public abstract void updateModelFromDTO(UserUpdateDTO dto, @MappingTarget User model);
}
