package hexlet.code.spring.mapper;

import hexlet.code.spring.dto.label.LabelCreateDTO;
import hexlet.code.spring.dto.label.LabelDTO;
import hexlet.code.spring.dto.label.LabelUpdateDTO;
import hexlet.code.spring.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMainMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
    public abstract LabelDTO mapToDTO(Label model);

    public abstract Label mapToModel(LabelCreateDTO dto);

    public abstract void updateModelFromDTO(LabelUpdateDTO dto, @MappingTarget Label model);
}
