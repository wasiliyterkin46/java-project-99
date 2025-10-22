package hexlet.code.spring.util;

import hexlet.code.spring.config.TestBeansConfig;
import hexlet.code.spring.model.Label;
import hexlet.code.spring.model.Task;
import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Getter
@Component
@Import(TestBeansConfig.class)
public class ModelGenerator {
    private Model<Task> taskModel;
    private Model<TaskStatus> taskStatusModel;
    private Model<User> userModel;
    private Model<Label> labelModel;

    private final Faker faker;

    private final int minLenText = 5;
    private final int maxLenText = 15;

    @Autowired
    public ModelGenerator(@NonNull final Faker fakerD) {
        this.faker = fakerD;
    }

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
                .supply(Select.field(Task::getLabels), () -> new HashSet<Label>())
                .toModel();

        labelModel = Instancio.of(Label.class).ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), () -> faker.text().text(minLenText, maxLenText,
                        false, false, false))
                .supply(Select.field(Label::getTasks), () -> new HashSet<Task>())
                .toModel();
    }
}
