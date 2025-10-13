package hexlet.code.spring.util;

import hexlet.code.spring.repository.LabelRepository;
import hexlet.code.spring.repository.TaskRepository;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.function.Function;

@Component
public final class TestUtils {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    public <T> Long getNonExistentId(final JpaRepository<T, Long> repository,
                                            final Function<T, Long> function) {
        return repository.findAll().stream().map(function).max(Comparator.naturalOrder()).orElse(0L) + 1;
    }

    public void clearAllRepository() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }
}
