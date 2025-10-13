package hexlet.code.spring.service;

import hexlet.code.spring.dto.taskstatus.TaskStatusCreateDTO;
import hexlet.code.spring.dto.taskstatus.TaskStatusDTO;
import hexlet.code.spring.dto.taskstatus.TaskStatusUpdateDTO;
import hexlet.code.spring.exception.DeleteRelatedEntityException;
import hexlet.code.spring.exception.RequestDataCannotBeProcessed;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.mapper.JsonNullableMapper;
import hexlet.code.spring.mapper.TaskStatusMainMapper;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository repository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusMainMapper mapper;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    public final List<TaskStatusDTO> getAll(final long start, final long end, final String order, final String sort) {
        return repository.findAll().stream()
                .sorted(getCompare(order, sort))
                .skip(start)
                .limit(end - start + 1)
                .map(mapper::mapToDTO)
                .toList();
    }

    public final TaskStatusDTO findById(final Long id) {
        var taskStatus = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Task status with id = %s not found", id)));
        return mapper.mapToDTO(taskStatus);
    }

    public final TaskStatusDTO create(@Valid final TaskStatusCreateDTO dto) {
        var taskStatus = mapper.mapToModel(dto);
        repository.save(taskStatus);
        return mapper.mapToDTO(taskStatus);
    }

    public final TaskStatusDTO update(@Valid final TaskStatusUpdateDTO dto, final Long id) {
        var taskStatus = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Task status with id = %s not found", id)));

        mapper.updateModelFromDTO(dto, taskStatus);
        repository.save(taskStatus);
        return mapper.mapToDTO(taskStatus);
    }

    public final void delete(final Long id) {
        var taskStatus = repository.findById(id);
        if (taskStatus.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Task status with id = %s not found", id));
        }

        var statusIsAssingedOnTask = taskRepository.existsByTaskStatus(taskStatus.get());
        if (statusIsAssingedOnTask) {
            throw new DeleteRelatedEntityException(String.format("Task status with id = %s is used in tasks", id));
        }

        repository.deleteById(id);
    }

    public final long count() {
        return repository.count();
    }

    private Comparator<TaskStatus> getCompare(final String order, final String sort) {
        Map<String, Comparator<TaskStatus>> mapComparator = new HashMap<>();
        mapComparator.put("id", Comparator.comparingLong(TaskStatus::getId));
        mapComparator.put("name", Comparator.comparing(TaskStatus::getName));
        mapComparator.put("slug", Comparator.comparing(TaskStatus::getSlug));
        mapComparator.put("createdAt", Comparator.comparing(TaskStatus::getCreatedAt));

        Map<String, Function<Comparator<TaskStatus>, Comparator<TaskStatus>>> mapOrder = new HashMap<>();
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
