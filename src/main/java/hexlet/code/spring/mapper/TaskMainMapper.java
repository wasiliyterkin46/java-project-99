package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.task.TaskCreateDTO;
import hexlet.code.spring.dto.task.TaskDTO;
import hexlet.code.spring.dto.task.TaskParamsDTO;
import hexlet.code.spring.dto.task.TaskUpdateDTO;
import hexlet.code.spring.model.Label;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.repository.LabelRepository;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
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
    public final void afterMapToParamsDTO(final Map<String, String> map, @MappingTarget final TaskParamsDTO dto) {
        dto.setStart(!map.containsKey("_start") ? 0L : Long.parseLong(map.get("_start")));
        dto.setEnd(!map.containsKey("_end") ? maxEntity : Long.parseLong(map.get("_end")));
    }

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    @Mapping(source = "labels", target = "taskLabelIds", qualifiedByName = "labelsToIds")
    @Mapping(source = "taskStatus", target = "status", qualifiedByName = "taskStatusToSlug")
    public abstract TaskDTO mapToDTO(Task model);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract Task mapToModel(TaskCreateDTO dto);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract void updateModelFromDTO(TaskUpdateDTO dto, @MappingTarget Task model);

    @Mapping(target = "start", ignore = true)
    @Mapping(target = "end", ignore = true)
    @Mapping(source = "titleCont", target = "titleCont")
    @Mapping(source = "assigneeId", target = "assigneeId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "labelId", target = "labelId")
    @Mapping(source = "_sort", target = "sortField", defaultValue = "id")
    @Mapping(source = "_order", target = "sortOrder", defaultValue = "ASC")
    public abstract TaskParamsDTO mapToParamDTO(Map<String, String> params);

    @Named("labelsToIds")
    static Set<Long> labelsToIds(final Set<Label> labels) {
        return labels == null
                ? new HashSet<>(Collections.emptyList())
                : new HashSet<>(labels.stream()
                        .map(Label::getId)
                        .toList());
    }

    @Named("taskStatusToSlug")
    static String taskStatusToSlug(final TaskStatus taskStatus) {
        return taskStatus.getSlug();
    }
}
