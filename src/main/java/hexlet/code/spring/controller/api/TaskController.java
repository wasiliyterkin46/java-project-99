package hexlet.code.spring.controller.api;

import hexlet.code.spring.dto.task.TaskCreateDTO;
import hexlet.code.spring.dto.task.TaskDTO;
import hexlet.code.spring.dto.task.TaskUpdateDTO;
import hexlet.code.spring.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService service;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> index(@RequestParam final Map<String, String> allParams) {
        var tasksDTO = service.getAll(allParams);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(service.count(allParams)))
                .body(tasksDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO show(@PathVariable final long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@Valid @RequestBody final TaskCreateDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO update(@Valid @RequestBody final TaskUpdateDTO dto, @PathVariable final long id) {
        return service.update(dto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id) {
        service.delete(id);
    }
}
