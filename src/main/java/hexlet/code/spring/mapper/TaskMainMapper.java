package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.task.TaskCreateDTO;
import hexlet.code.spring.dto.task.TaskDTO;
import hexlet.code.spring.dto.task.TaskUpdateDTO;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.repository.TaskStatusRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMainMapper {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @AfterMapping
    public final void addStatusToModel(final TaskCreateDTO dto, @MappingTarget final Task model) {
        var taskStatusName = dto.getStatus();
        var taskStatus = taskStatusRepository.findByName(taskStatusName);
        model.setTaskStatus(taskStatus.orElseThrow(() -> {
            throw new ResourceNotFoundException(String.format(
                    "Task status with name = %s not found", dto.getStatus()));
        }));
    }

    @AfterMapping
    public final void addStatusToModel(final TaskUpdateDTO dto, @MappingTarget final Task model) {
        if (jsonNullableMapper.isPresent(dto.getStatus())) {
            var taskStatusName = jsonNullableMapper.unwrap(dto.getStatus());
            var taskStatus = taskStatusRepository.findByName(taskStatusName);

            model.setTaskStatus(taskStatus.orElseThrow(() -> {
                throw new ResourceNotFoundException(String.format(
                        "Task status with name = %s not found", dto.getStatus()));
            }));
        }
    }

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "taskStatus.name", target = "status")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract TaskDTO mapToDTO(Task model);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(target = "taskStatus", ignore = true)
    public abstract Task mapToModel(TaskCreateDTO dto);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(target = "taskStatus", ignore = true)
    public abstract void updateModelFromDTO(TaskUpdateDTO dto, @MappingTarget Task model);
}
