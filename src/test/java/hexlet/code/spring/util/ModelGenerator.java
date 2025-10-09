package hexlet.code.spring.util;

import hexlet.code.spring.config.TestBeansConfig;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Getter
@Component
@Import(TestBeansConfig.class)
public class ModelGenerator {
    private Model<Task> taskModel;
    private Model<TaskStatus> taskStatusModel;
    private Model<User> userModel;

    @Autowired
    private Faker faker;

    private final int minLenText = 5;
    private final int maxLenText = 15;

    @PostConstruct
    private void init() {
        taskStatusModel = Instancio.of(TaskStatus.class).ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.text().text(minLenText, maxLenText,
                        false, false, false))
                .supply(Select.field(TaskStatus::getSlug), () -> faker.text().text(minLenText, maxLenText,
                        false, false, false))
                .toModel();

        userModel = Instancio.of(User.class).ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress()).toModel();

        taskModel = Instancio.of(Task.class).ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getTaskStatus), () -> null)
                .supply(Select.field(Task::getName), () -> faker.hacker().verb())
                .supply(Select.field(Task::getAssignee), () -> null)
                .toModel();
    }
}
