package hexlet.code.spring.util;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Comparator;
import java.util.function.Function;

public final class TestUtils {
    private TestUtils() { }

    public static <T> Long getNonExistentId(final JpaRepository<T, Long> repository,
                                            final Function<T, Long> function) {
        return repository.findAll().stream().map(function).max(Comparator.naturalOrder()).orElse(0L) + 1;
    }
}
