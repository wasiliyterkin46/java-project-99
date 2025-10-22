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
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class TaskStatusService {

    private final TaskStatusRepository repository;
    private final TaskRepository taskRepository;
    private final TaskStatusMainMapper mapper;
    private final JsonNullableMapper jsonNullableMapper;

    private final String rightOrder = "ASC";
    private final String inverseOrder = "DESC";

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
        var slug = dto.getSlug();
        if (repository.existsBySlug(slug)) {
            throw new RequestDataCannotBeProcessed(String.format("Slug должен быть уникальным. "
                    + "В базе уже есть статус задачи со slug = %s", slug));
        }

        var taskStatus = mapper.mapToModel(dto);
        repository.save(taskStatus);
        return mapper.mapToDTO(taskStatus);
    }

    public final TaskStatusDTO update(@Valid final TaskStatusUpdateDTO dto, final Long id) {
        var taskStatus = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Task status with id = %s not found", id)));

        if (jsonNullableMapper.isPresent(dto.getSlug())) {
            var slug = jsonNullableMapper.unwrap(dto.getSlug());
            if (repository.existsBySlug(slug)) {
                throw new RequestDataCannotBeProcessed(String.format("Slug должен быть уникальным. "
                        + "В базе уже есть статус задачи со slug = %s", slug));
            }
        }

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
        mapOrder.put(inverseOrder, Comparator::reversed);
        mapOrder.put(rightOrder, comp -> comp);

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
