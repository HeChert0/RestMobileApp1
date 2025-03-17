package app.mapper;

import app.dto.OrderDTO;
import app.models.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper extends BaseMapper<Order, OrderDTO> {
}
