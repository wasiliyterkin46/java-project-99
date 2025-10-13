package hexlet.code.spring.service;

import hexlet.code.spring.dto.label.LabelCreateDTO;
import hexlet.code.spring.dto.label.LabelDTO;
import hexlet.code.spring.dto.label.LabelUpdateDTO;
import hexlet.code.spring.exception.DeleteRelatedEntityException;
import hexlet.code.spring.exception.RequestDataCannotBeProcessed;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.mapper.LabelMainMapper;
import hexlet.code.spring.model.Label;
import hexlet.code.spring.repository.LabelRepository;
import hexlet.code.spring.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class LabelService {

    @Autowired
    private LabelRepository repository;

    @Autowired
    private LabelMainMapper mapper;

    @Autowired
    private TaskRepository taskRepository;

    public final List<LabelDTO> getAll(final long start, final long end, final String order, final String sort) {
        return repository.findAll().stream()
                .sorted(getCompare(order, sort))
                .skip(start)
                .limit(end - start + 1)
                .map(mapper::mapToDTO)
                .toList();
    }

    public final LabelDTO findById(final Long id) {
        var label = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Label with id = %s not found", id)));
        return mapper.mapToDTO(label);
    }

    public final LabelDTO create(@Valid final LabelCreateDTO dto) {
        var label = mapper.mapToModel(dto);
        repository.save(label);
        return mapper.mapToDTO(label);
    }

    public final LabelDTO update(@Valid final LabelUpdateDTO dto, final Long id) {
        var label = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Label with id = %s not found", id)));

        mapper.updateModelFromDTO(dto, label);
        repository.save(label);
        return mapper.mapToDTO(label);
    }

    public final void delete(final Long id) {
        var label = repository.findById(id);
        if (label.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Label with id = %s not found", id));
        }

        var labelIsUsedInTask = taskRepository.existsByLabelsId(label.get().getId());
        if (labelIsUsedInTask) {
            throw new DeleteRelatedEntityException(String.format("Label with id = %s is used in tasks", id));
        }

        repository.deleteById(id);
    }

    public final long count() {
        return repository.count();
    }

    private Comparator<Label> getCompare(final String order, final String sort) {
        Map<String, Comparator<Label>> mapComparator = new HashMap<>();
        mapComparator.put("id", Comparator.comparingLong(Label::getId));
        mapComparator.put("name", Comparator.comparing(Label::getName));
        mapComparator.put("createdAt", Comparator.comparing(Label::getCreatedAt));

        Map<String, Function<Comparator<Label>, Comparator<Label>>> mapOrder = new HashMap<>();
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
