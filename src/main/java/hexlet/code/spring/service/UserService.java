package hexlet.code.spring.service;

import hexlet.code.spring.dto.user.UserCreateDTO;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.dto.user.UserUpdateDTO;
import hexlet.code.spring.exception.ResourceNotFoundException;
import hexlet.code.spring.mapper.UserMainMapper;
import hexlet.code.spring.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private UserMainMapper mapper;

    public final List<UserDTO> getAll() {
        var users = repository.findAll();
        return users.stream()
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
}
