package hexlet.code.spring.service;

import hexlet.code.spring.dto.taskstatus.TaskStatusCreateDTO;
import hexlet.code.spring.dto.taskstatus.TaskStatusDTO;
import hexlet.code.spring.dto.taskstatus.TaskStatusUpdateDTO;
import hexlet.code.spring.exception.DuplicateDataException;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.mapper.JsonNullableMapper;
import hexlet.code.spring.mapper.TaskStatusMainMapper;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository repository;

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

    public final TaskStatusDTO create(final TaskStatusCreateDTO dto) {
        var slug = dto.getSlug();
        if (repository.existsBySlug(slug)) {
            throw new DuplicateDataException(String.format("Slug должен быть уникальным. "
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
                throw new DuplicateDataException(String.format("Slug должен быть уникальным. "
                        + "В базе уже есть статус задачи со slug = %s", slug));
            }
        }

        mapper.updateModelFromDTO(dto, taskStatus);
        repository.save(taskStatus);
        return mapper.mapToDTO(taskStatus);
    }

    public final void delete(final Long id) {
        var taskStatusExist = repository.existsById(id);
        if (!taskStatusExist) {
            throw new ResourceNotFoundException(String.format("Task status with id = %s not found", id));
        }
        repository.deleteById(id);
    }

    public final long count() {
        return repository.count();
    }

    private Comparator<TaskStatus> getCompare(final String order, final String sort) {
        Comparator<TaskStatus> comparator = null;
        switch (sort) {
            case "id":
                comparator = Comparator.comparingLong(TaskStatus::getId);
                break;
            case "name":
                comparator = Comparator.comparing(TaskStatus::getName);
                break;
            case "slug":
                comparator = Comparator.comparing(TaskStatus::getSlug);
                break;
            case "createdAt":
                comparator = Comparator.comparing(TaskStatus::getCreatedAt);
                break;
            default:
                throw new IllegalArgumentException(String.format("Указано некорректное поле сортировки = %s", sort));
        }

        switch (order) {
            case "DESC":
                comparator = comparator.reversed();
                break;
            case "ASC":
                break;
            default:
                throw new IllegalArgumentException(String.format("Указан некорректный порядок сортировки = %s", order));
        }

        return comparator;
    }
}
