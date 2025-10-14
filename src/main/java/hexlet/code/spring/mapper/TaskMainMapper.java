package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.task.TaskCreateDTO;
import hexlet.code.spring.dto.task.TaskDTO;
import hexlet.code.spring.dto.task.TaskParamsDTO;
import hexlet.code.spring.dto.task.TaskUpdateDTO;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.model.Label;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.repository.LabelRepository;
import hexlet.code.spring.repository.TaskRepository;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mapper(uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {}
)
public abstract class TaskMainMapper {
    private final long maxEntity = 10L;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository repository;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @AfterMapping
    public final void afterMapToDTO(final Task model, @MappingTarget final TaskDTO dto) {
        // labels
        var labels = labelRepository.findAllByTasks(model);
        Set<Long> labelIds = new HashSet<>(labels.stream().map(Label::getId).toList());
        dto.setLabelIds(labelIds);

        // status.name
        var statusId = model.getTaskStatus().getId();
        var statusName = taskStatusRepository.findById(statusId).get().getName();
        dto.setStatus(statusName);
    }

    @AfterMapping
    public final void afterCreate(final TaskCreateDTO dto, @MappingTarget final Task model) {
        // Status
        var taskStatusName = dto.getStatus();
        var taskStatus = taskStatusRepository.findByName(taskStatusName).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Task with name = %s not found",
                        taskStatusName)));
        model.setTaskStatus(taskStatus);

        // Assignee
        Long userId = dto.getAssigneeId();
        if (userId != null) {
            var user = userRepository.findById(userId).orElseThrow(() ->
                    new ResourceNotFoundException(String.format("User-assignee with id = %s not found", userId)));
            model.setAssignee(user);
        }

        // Labels
        var ids = dto.getLabelIds();
        if (ids != null && !ids.isEmpty()) {
            var labels = labelRepository.findAllById(ids);
            if (labels.size() != ids.size()) {
                throw new ResourceNotFoundException("Some labels in task not found");
            }
            model.setLabels(new HashSet<>(labels));
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

            if (userId != null) {
                var user = userRepository.findById(userId).orElseThrow(() ->
                        new ResourceNotFoundException(String.format("User-assignee with id = %s not found", userId)));
                model.setAssignee(user);
            } else {
                model.setAssignee(null);
            }
        }

        if (jsonNullableMapper.isPresent(dto.getLabelIds())) {
            var ids = jsonNullableMapper.unwrap(dto.getLabelIds());
            if (ids != null && !ids.isEmpty()) {
                var labels = labelRepository.findAllById(ids);
                if (labels.size() != ids.size()) {
                    throw new ResourceNotFoundException("Some labels in task not found");
                }
                model.setLabels(new HashSet<>(labels));
            } else {
                model.setLabels(new HashSet<>());
            }
        }
    }

    @AfterMapping
    public final void afterMapToParamsDTO(final Map<String, String> map, @MappingTarget final TaskParamsDTO dto) {
        dto.setStart(!map.containsKey("_start") ? 0L : Long.parseLong(map.get("_start")));
        dto.setEnd(!map.containsKey("_end") ? maxEntity : Long.parseLong(map.get("_end")));
        dto.setSortField(!map.containsKey("_sort") ? "id" : map.get("_sort"));
        dto.setSortOrder(!map.containsKey("_order") ? "ASC" : map.get("_order"));
        if (map.containsKey("titleCont")) {
            dto.setTitleCont(map.get("titleCont"));
        }
        if (map.containsKey("status")) {
            dto.setStatus(map.get("status"));
        }
        if (map.containsKey("assigneeId")) {
            dto.setAssigneeId(Long.parseLong(map.get("assigneeId")));
        }
        if (map.containsKey("labelId")) {
            dto.setLabelId(Long.parseLong(map.get("labelId")));
        }
    }

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "labelIds", ignore = true)
    @Mapping(target = "status", ignore = true)
    public abstract TaskDTO mapToDTO(Task model);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "assigneeId", target = "assignee.id")
    @Mapping(target = "taskStatus.id", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract Task mapToModel(TaskCreateDTO dto);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract void updateModelFromDTO(TaskUpdateDTO dto, @MappingTarget Task model);

    @Mapping(target = "titleCont", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sortField", ignore = true)
    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "assigneeId", ignore = true)
    @Mapping(target = "labelId", ignore = true)
    @Mapping(target = "start", ignore = true)
    @Mapping(target = "end", ignore = true)
    public abstract TaskParamsDTO mapToParamDTO(Map<String, String> params);
}
