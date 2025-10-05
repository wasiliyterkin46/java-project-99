package hexlet.code.spring.mapper;

import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class JsonNullableMapper {
    public final <T> JsonNullable<T> wrap(final T entity) {
        return JsonNullable.of(entity);
    }

    public final <T> T unwrap(final JsonNullable<T> jsonNullable) {
        return jsonNullable == null ? null : jsonNullable.orElse(null);
    }

    @Condition
    public final <T> boolean isPresent(final JsonNullable<T> nullable) {
        return nullable != null && nullable.isPresent();
    }
}
