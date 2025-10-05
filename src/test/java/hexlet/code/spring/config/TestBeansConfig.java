package hexlet.code.spring.config;

import net.datafaker.Faker;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestBeansConfig {
    @Bean
    public Faker getFaker() {
        return new Faker();
    }
}
