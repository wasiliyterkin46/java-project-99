package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.task.TaskCreateDTO;
import hexlet.code.spring.dto.task.TaskDTO;
import hexlet.code.spring.dto.task.TaskUpdateDTO;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @AfterMapping
    public final void afterMapToDTO(final Task model, @MappingTarget final TaskDTO dto) {
        if (model.getTaskStatus() != null) {
            var taskStatusName = taskStatusRepository.findById(model.getTaskStatus().getId()).orElseThrow(() ->
                    new ResourceNotFoundException(String.format("Task with id = %s not found",
                            model.getTaskStatus().getId()))).getName();
            dto.setStatus(taskStatusName);
        }

        if (model.getAssignee() != null) {
            dto.setAssigneeId(model.getAssignee().getId());
        }
    }

    @AfterMapping
    public final void afterCreate(final TaskCreateDTO dto, @MappingTarget final Task model) {
        var taskStatusName = dto.getStatus();
        var taskStatus = taskStatusRepository.findByName(taskStatusName).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Task status with name = %s not found", taskStatusName)));
        model.setTaskStatus(taskStatus);

        var userId = dto.getAssigneeId();
        if (userId != null) {
            var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(String.format(
                    "User with id = %s not found", userId)));
            model.setAssignee(user);
        } else {
            model.setAssignee(null);
        }
    }

    @AfterMapping
    public final void afterUpdate(final TaskUpdateDTO dto, @MappingTarget final Task model) {
        if (jsonNullableMapper.isPresent(dto.getStatus())) {
            var taskStatusName = jsonNullableMapper.unwrap(dto.getStatus());
            var taskStatus = taskStatusRepository.findByName(taskStatusName).orElseThrow(() ->
                    new ResourceNotFoundException(String.format("Task status with name"
                            + " = %s not found", taskStatusName)));

            model.setTaskStatus(taskStatus);
        }

        if (jsonNullableMapper.isPresent(dto.getAssigneeId())) {
            var userId = jsonNullableMapper.unwrap(dto.getAssigneeId());
            var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(String.format(
                    "User with id = %s not found", userId)));

            model.setAssignee(user);
        }
    }

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
//    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(target = "assigneeId", ignore = true)
//    @Mapping(source = "taskStatus.name", target = "status")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract TaskDTO mapToDTO(Task model);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
//    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    public abstract Task mapToModel(TaskCreateDTO dto);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
//    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    public abstract void updateModelFromDTO(TaskUpdateDTO dto, @MappingTarget Task model);
}
