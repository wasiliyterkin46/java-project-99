package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.taskstatus.TaskStatusCreateDTO;
import hexlet.code.spring.dto.taskstatus.TaskStatusDTO;
import hexlet.code.spring.dto.taskstatus.TaskStatusUpdateDTO;
import hexlet.code.spring.exception.RequestDataCannotBeProcessed;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.repository.TaskStatusRepository;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskStatusMainMapper {

    @Autowired
    private TaskStatusRepository repository;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @BeforeMapping
    public final void beforeCreate(final TaskStatusCreateDTO dto, @MappingTarget final TaskStatus model) {
        var slug = dto.getSlug();
        if (repository.existsBySlug(slug)) {
            throw new RequestDataCannotBeProcessed(String.format("Slug должен быть уникальным. "
                    + "В базе уже есть статус задачи со slug = %s", slug));
        }
    }

    @BeforeMapping
    public final void beforeUpdate(final TaskStatusUpdateDTO dto, @MappingTarget final TaskStatus model) {
        if (jsonNullableMapper.isPresent(dto.getSlug())) {
            var slug = jsonNullableMapper.unwrap(dto.getSlug());
            if (repository.existsBySlug(slug)) {
                throw new RequestDataCannotBeProcessed(String.format("Slug должен быть уникальным. "
                        + "В базе уже есть статус задачи со slug = %s", slug));
            }
        }
    }

    @Mapping(target = "createdAt", ignore = true)
    public abstract TaskStatus mapToModel(TaskStatusDTO dto);

    public abstract TaskStatus mapToModel(TaskStatusCreateDTO dto);

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract TaskStatusDTO mapToDTO(TaskStatus model);

    public abstract void updateModelFromDTO(TaskStatusUpdateDTO dto, @MappingTarget TaskStatus model);
}
