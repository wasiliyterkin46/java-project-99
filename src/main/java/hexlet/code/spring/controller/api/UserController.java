package hexlet.code.spring.controller.api;

import hexlet.code.spring.dto.user.UserCreateDTO;
import hexlet.code.spring.dto.user.UserDTO;
import hexlet.code.spring.dto.user.UserUpdateDTO;
import hexlet.code.spring.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public final class UserController {

    private final UserService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserDTO>> index(@RequestParam(name = "_start", defaultValue = "0") final long start,
                               @RequestParam(name = "_end", defaultValue = "10") final long end,
                               @RequestParam(name = "_order", defaultValue = "ASC") final String order,
                               @RequestParam(name = "_sort", defaultValue = "id") final String sort) {
        var usersDTO = service.getAll(start, end, order, sort);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(service.count()))
                .body(usersDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable final long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody final UserCreateDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@Valid @RequestBody final UserUpdateDTO dto, @PathVariable final long id) {
        return service.update(dto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id) {
        service.delete(id);
    }
}
