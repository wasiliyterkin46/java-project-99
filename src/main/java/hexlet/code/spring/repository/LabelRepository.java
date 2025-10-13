package hexlet.code.spring.repository;

import hexlet.code.spring.model.Label;
import hexlet.code.spring.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByName(String name);
    boolean existsByName(String name);
    List<Label> findAllByTasks(Task task);
}
