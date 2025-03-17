package app.mapper;

import app.dto.SmartphoneDto;
import app.models.Smartphone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SmartphoneMapper extends BaseMapper<Smartphone, SmartphoneDto> {

    @SuppressWarnings("checkstyle:Indentation")
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "brand", source = "brand"),
            @Mapping(target = "model", source = "model"),
            @Mapping(target = "price", source = "price")
    })
    @Override
    Smartphone toEntity(SmartphoneDto dto);

    @Override
    SmartphoneDto toDto(Smartphone smartphone);
}
