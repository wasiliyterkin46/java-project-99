package hexlet.code.spring.service;

import hexlet.code.spring.dto.user.UserCreateDTO;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.dto.user.UserUpdateDTO;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.mapper.UserMainMapper;
import hexlet.code.spring.model.User;
import hexlet.code.spring.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private UserMainMapper mapper;

    public final List<UserDTO> getAll(final long start, final long end, final String order, final String sort) {
        return repository.findAll().stream()
                .sorted(getCompare(order, sort))
                .skip(start)
                .limit(end - start + 1)
                .map(mapper::map)
                .toList();
    }

    public final UserDTO findById(final Long id) {
        var user = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("User with id = %s not found", id)));
        return mapper.map(user);
    }

    public final UserDTO create(final UserCreateDTO dto) {
        var user = mapper.map(dto);
        repository.save(user);
        return mapper.map(user);
    }

    public final UserDTO update(@Valid final UserUpdateDTO dto, final Long id) {
        var user = repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("User with id = %s not found", id)));
        mapper.update(dto, user);
        repository.save(user);
        return mapper.map(user);
    }

    public final void delete(final Long id) {
        var userExist = repository.existsById(id);
        if (!userExist) {
            throw new ResourceNotFoundException(String.format("User with id = %s not found", id));
        }
        repository.deleteById(id);
    }

    public final long count() {
        return repository.count();
    }

    private Comparator<User> getCompare(final String order, final String sort) {
        Comparator<User> comparator = null;
        switch (sort) {
            case "id":
                comparator = Comparator.comparingLong(User::getId);
                break;
            case "email":
                comparator = Comparator.comparing(User::getEmail);
                break;
            case "firstName":
                comparator = Comparator.comparing(User::getFirstName, Comparator.nullsFirst(String::compareTo));
                break;
            case "lastName":
                comparator = Comparator.comparing(User::getLastName, Comparator.nullsFirst(String::compareTo));
                break;
            case "createdAt":
                comparator = Comparator.comparing(User::getCreatedAt);
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
