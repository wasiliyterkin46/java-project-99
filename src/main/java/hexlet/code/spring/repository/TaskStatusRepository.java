package hexlet.code.spring.repository;

import hexlet.code.spring.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
