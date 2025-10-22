package hexlet.code.spring.service;

import hexlet.code.spring.dto.user.UserCreateDTO;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.dto.user.UserUpdateDTO;
import hexlet.code.spring.exception.DeleteRelatedEntityException;
import hexlet.code.spring.exception.RequestDataCannotBeProcessed;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.mapper.JsonNullableMapper;
import hexlet.code.spring.mapper.UserMainMapper;
import hexlet.code.spring.model.User;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final TaskRepository taskRepository;
    private final UserMainMapper mapper;
    private final JsonNullableMapper jsonNullableMapper;
    private final PasswordEncoder encoder;

    private final String rightOrder = "ASC";
    private final String inverseOrder = "DESC";

    public final List<UserDTO> getAll(final long start, final long end, final String order, final String sort) {
        return repository.findAll().stream()
                .sorted(getCompare(order, sort))
                .skip(start)
                .limit(end - start + 1)
                .map(mapper::mapToDTO)
                .toList();
    }

    public final UserDTO findById(final Long id) {
        var user = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("User with id = %s not found", id)));
        return mapper.mapToDTO(user);
    }

    public final UserDTO create(final UserCreateDTO dto) {
        var email = dto.getEmail();
        if (repository.existsByEmail(email)) {
            throw new RequestDataCannotBeProcessed(String.format("Email должен быть уникальным. "
                    + "В базе уже есть пользователь с email = %s", email));
        }

        var password = dto.getPassword();
        dto.setPassword(encoder.encode(password));

        var user = mapper.mapToModel(dto);
        repository.save(user);
        return mapper.mapToDTO(user);
    }

    public final UserDTO update(@Valid final UserUpdateDTO dto, final Long id) {
        var user = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("User with id = %s not found", id)));

        if (jsonNullableMapper.isPresent(dto.getEmail())) {
            var email = jsonNullableMapper.unwrap(dto.getEmail());
            if (repository.existsByEmail(email)) {
                throw new RequestDataCannotBeProcessed(String.format("Email должен быть уникальным. "
                        + "В базе уже есть пользователь с email = %s", email));
            }
        }

        mapper.updateModelFromDTO(dto, user);

        if (jsonNullableMapper.isPresent(dto.getPassword())) {
            user.setPasswordDigest(encoder.encode(jsonNullableMapper.unwrap(dto.getPassword())));
        }

        repository.save(user);
        return mapper.mapToDTO(user);
    }

    public final void delete(final Long id) {
        var user = repository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException(String.format("User with id = %s not found", id));
        }

        var userIsAssingedOnTask = taskRepository.existsByAssignee(user.get());
        if (userIsAssingedOnTask) {
            throw new DeleteRelatedEntityException(String.format("User with id = %s "
                    + "is assigned as the task executor", id));
        }

        repository.deleteById(id);
    }

    public final long count() {
        return repository.count();
    }

    private Comparator<User> getCompare(final String order, final String sort) {
        Map<String, Comparator<User>> mapComparator = new HashMap<>();
        mapComparator.put("id", Comparator.comparingLong(User::getId));
        mapComparator.put("email", Comparator.comparing(User::getEmail));
        mapComparator.put("firstName", Comparator.comparing(User::getFirstName,
                Comparator.nullsFirst(String::compareTo)));
        mapComparator.put("lastName", Comparator.comparing(User::getLastName,
                Comparator.nullsFirst(String::compareTo)));
        mapComparator.put("createdAt", Comparator.comparing(User::getCreatedAt));

        Map<String, Function<Comparator<User>, Comparator<User>>> mapOrder = new HashMap<>();
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
