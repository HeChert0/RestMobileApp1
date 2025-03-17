package app.mapper;

import app.dto.SmartphoneDTO;
import app.models.Smartphone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SmartphoneMapper extends BaseMapper<Smartphone, SmartphoneDTO> {

    // Настроим маппинг для поля orderId:
    @Mapping(target = "orderId", source = "order.id")
    SmartphoneDTO toDto(Smartphone smartphone);

    // Для обратного преобразования, если нужно установить order по ID, это можно обработать отдельно\n
    // Если orderId не нужен для создания entity, можно оставить стандартный маппинг\n
}
