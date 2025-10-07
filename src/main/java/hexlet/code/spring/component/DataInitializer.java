package hexlet.code.spring.component;

import hexlet.code.spring.model.TaskStatus;
import hexlet.code.spring.model.User;
import hexlet.code.spring.repository.TaskStatusRepository;
import hexlet.code.spring.repository.UserRepository;
import hexlet.code.spring.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public final class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final CustomUserDetailsService userService;

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        if (!userRepository.existsByEmail("hexlet@example.com")) {
            userInit();
        }

        Optional<String> profile = Optional.ofNullable(System.getenv("SPRING_PROFILES_ACTIVE"));
        if (profile.isEmpty() || !profile.get().equals("production")) {
            taskStatusInit();
        }

    }

    private void userInit() {
        var userData = new User();
        userData.setEmail("hexlet@example.com");
        userData.setPasswordDigest("qwerty");
        userService.createUser(userData);
    }

    private void taskStatusInit() {
        List<String> list = Arrays.asList("draft", "to_review", "to_be_fixed", "to_publish", "published");
        for (String elem : list) {
            var taskStatus = new TaskStatus();
            taskStatus.setName(elem);
            taskStatus.setSlug(elem);
            taskStatusRepository.save(taskStatus);
        }
    }
}
