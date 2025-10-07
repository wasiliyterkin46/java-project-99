package hexlet.code.spring.util;

import hexlet.code.spring.config.TestBeansConfig;
import hexlet.code.spring.model.TaskStatus;
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
public class TaskStatusModelGenerator {
    private Model<TaskStatus> taskStatusModel;

    @Autowired
    private Faker faker;

    @PostConstruct
    private void init() {
        taskStatusModel = Instancio.of(TaskStatus.class).ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.hobbit().character())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.hobbit().location())
                .toModel();
    }
}
