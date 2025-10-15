package hexlet.code.spring.service;

import hexlet.code.spring.dto.task.TaskCreateDTO;
import hexlet.code.spring.dto.task.TaskDTO;
import hexlet.code.spring.dto.task.TaskUpdateDTO;
import hexlet.code.spring.exception.RequestDataCannotBeProcessed;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.mapper.JsonNullableMapper;
import hexlet.code.spring.mapper.TaskMainMapper;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.repository.LabelRepository;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
import hexlet.code.spring.specification.TaskSpecification;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class TaskService {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskMainMapper mapper;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @Autowired
    private TaskSpecification specification;

    public final List<TaskDTO> getAll(final Map<String, String> params) {
        var paramsDTO = mapper.mapToParamDTO(params);
        var spec = specification.build(paramsDTO);

        return repository.findAll(spec).stream()
                .sorted(getCompare(paramsDTO.getSortOrder(), paramsDTO.getSortField()))
                .skip(paramsDTO.getStart())
                .limit(paramsDTO.getEnd() - paramsDTO.getStart() + 1)
                .map(mapper::mapToDTO)
                .toList();
    }

    public final TaskDTO findById(final Long id) {
        var task = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Task with id = %s not found", id)));
        return mapper.mapToDTO(task);
    }

    public final TaskDTO create(@Valid final TaskCreateDTO dto) {
        var task = mapper.mapToModel(dto);
        repository.save(task);
        return mapper.mapToDTO(task);
    }

    public final TaskDTO update(@Valid final TaskUpdateDTO dto, final Long id) {
        var task = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Task with id = %s not found", id)));
        mapper.updateModelFromDTO(dto, task);
        repository.save(task);
        return mapper.mapToDTO(task);
    }

    public final void delete(final Long id) {
        var taskExist = repository.existsById(id);
        if (!taskExist) {
            throw new ResourceNotFoundException(String.format("Task with id = %s not found", id));
        }
        repository.deleteById(id);
    }

    public final long count() {
        return repository.count();
    }

    public final long count(final Map<String, String> params) {
        var paramsDTO = mapper.mapToParamDTO(params);
        var spec = specification.build(paramsDTO);
        return repository.findAll(spec).size();
    }

    private Comparator<Task> getCompare(final String order, final String sort) {
        Map<String, Comparator<Task>> mapComparator = new HashMap<>();
        mapComparator.put("index", Comparator.comparing(Task::getIndex, Comparator.nullsFirst(Integer::compareTo)));
        mapComparator.put("id", Comparator.comparingLong(Task::getId));
        mapComparator.put("name", Comparator.comparing(Task::getName));
        mapComparator.put("description", Comparator.comparing(Task::getDescription,
                Comparator.nullsFirst(String::compareTo)));
        mapComparator.put("createdAt", Comparator.comparing(Task::getCreatedAt));

        Map<String, Function<Comparator<Task>, Comparator<Task>>> mapOrder = new HashMap<>();
        mapOrder.put("DESC", Comparator::reversed);
        mapOrder.put("ASC", comp -> comp);

        if (!mapComparator.containsKey(sort)) {
            throw new RequestDataCannotBeProcessed(String.format("Указано некорректное поле сортировки = %s", sort));
        }
        if (!mapOrder.containsKey(order)) {
            throw new RequestDataCannotBeProcessed(String.format("Указан некорректный порядок сортировки = %s", order));
        }

        var comparator = mapComparator.get(sort);
        var func = mapOrder.get(order);

        return func.apply(comparator);
    }
}
