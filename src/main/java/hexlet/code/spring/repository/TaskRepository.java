package hexlet.code.spring.repository;

import hexlet.code.spring.model.Task;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    boolean existsByAssignee(User user);
    boolean existsByTaskStatus(TaskStatus taskStatus);
    boolean existsByAssigneeAndTaskStatus(User user, TaskStatus taskStatus);
    boolean existsByLabelsId(Long labelId);
}
