package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.label.LabelCreateDTO;
import hexlet.code.spring.dto.label.LabelDTO;
import hexlet.code.spring.dto.label.LabelUpdateDTO;
import hexlet.code.spring.exception.RequestDataCannotBeProcessed;
import hexlet.code.spring.model.Label;
import hexlet.code.spring.repository.LabelRepository;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMainMapper {
    @Autowired
    private LabelRepository repository;

    @BeforeMapping
    public final void beforeCreate(final LabelCreateDTO dto, @MappingTarget final Label model) {
        var nameLabel = dto.getName();
        if (repository.existsByName(nameLabel)) {
            throw new RequestDataCannotBeProcessed(String.format("Название метки должно быть уникальным. "
                    + "В базе уже есть метка с name = %s", nameLabel));
        }
    }

    @BeforeMapping
    public final void beforeUpdate(final LabelUpdateDTO dto, @MappingTarget final Label model) {
        var nameLabel = dto.getName();
        if (repository.existsByName(nameLabel)) {
            throw new RequestDataCannotBeProcessed(String.format("Название метки должно быть уникальным. "
                    + "В базе уже есть метка с name = %s", nameLabel));
        }
    }

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract LabelDTO mapToDTO(Label model);

    public abstract Label mapToModel(LabelCreateDTO dto);

    public abstract void updateModelFromDTO(LabelUpdateDTO dto, @MappingTarget Label model);
}
