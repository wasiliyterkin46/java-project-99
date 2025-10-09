package hexlet.code.spring.mapper;

import hexlet.code.spring.model.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ReferenceMapper {
    @PersistenceContext
    private EntityManager entityManager;

    public <T extends BaseEntity> T toEntity(final Long id, @TargetType final Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }
}
