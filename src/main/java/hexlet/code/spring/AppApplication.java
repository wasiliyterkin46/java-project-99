package hexlet.code.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public final class AppApplication {
    private AppApplication() { }

    public static void main(final String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
}
