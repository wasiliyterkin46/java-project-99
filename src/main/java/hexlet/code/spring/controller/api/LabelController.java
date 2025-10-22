package hexlet.code.spring.controller.api;

import hexlet.code.spring.dto.label.LabelCreateDTO;
import hexlet.code.spring.dto.label.LabelDTO;
import hexlet.code.spring.dto.label.LabelUpdateDTO;
import hexlet.code.spring.service.LabelService;
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
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelService service;

    @GetMapping
    public ResponseEntity<List<LabelDTO>> index(
            @RequestParam(name = "_start", defaultValue = "0") final long start,
            @RequestParam(name = "_end", defaultValue = "10") final long end,
            @RequestParam(name = "_order", defaultValue = "ASC") final String order,
            @RequestParam(name = "_sort", defaultValue = "id") final String sort) {
        var labelsDTO = service.getAll(start, end, order, sort);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(service.count()))
                .body(labelsDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO show(@PathVariable final long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody final LabelCreateDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO update(@Valid @RequestBody final LabelUpdateDTO dto, @PathVariable final long id) {
        return service.update(dto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final long id) {
        service.delete(id);
    }
}
