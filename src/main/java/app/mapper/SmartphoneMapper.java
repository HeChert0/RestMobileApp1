package app.mapper;

import app.dto.SmartphoneDto;
import app.models.Smartphone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SmartphoneMapper extends BaseMapper<Smartphone, SmartphoneDto> {

    @Mapping(target = "orderId", source = "order.id")
    SmartphoneDto toDto(Smartphone smartphone);
}
