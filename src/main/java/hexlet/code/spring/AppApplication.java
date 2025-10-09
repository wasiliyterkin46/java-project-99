package hexlet.code.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public final class AppApplication {
    private AppApplication() { }

    public static void main(final String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
}
