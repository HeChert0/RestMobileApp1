package app.mapper;

import app.dto.OrderDto;
import app.dto.SmartphoneDto;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface OrderMapper extends BaseMapper<Order, OrderDto> {

    @SuppressWarnings("checkstyle:Indentation")
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId"),
            @Mapping(target = "totalAmount", source = "totalAmount"),
            @Mapping(target = "orderDate", source = "orderDate"),
            @Mapping(target = "smartphones", ignore = true)
    })
    @Override
    Order toEntity(OrderDto dto);

    @SuppressWarnings({"checkstyle:Indentation", "checkstyle:OperatorWrap"})
    @Mappings({
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "smartphoneIds", expression =
                    "java(order.getSmartphones() == null ?" +
                            " new ArrayList<>() : " +
                            "order.getSmartphones().stream().map(app.models.Smartphone::getId)" +
                            ".collect(java.util.stream.Collectors.toList()))"),
            @Mapping(target = "totalAmount", source = "totalAmount"),
            @Mapping(target = "orderDate", source = "orderDate"),
            @Mapping(target = "smartphones", ignore = true)
    })
    @Override
    OrderDto toDto(Order order);

    List<OrderDto> toDtos(Iterable<Order> orders);

    @Named("userFromId")
    default User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при маппинге user id", e);
        }
        return user;
    }

    @AfterMapping
    default void fillSmartphones(@MappingTarget OrderDto dto, Order order) {

        if (order.getSmartphones() != null) {
            List<SmartphoneDto> smartphones = order.getSmartphones().stream()
                    .map(s -> org.mapstruct.factory.Mappers
                            .getMapper(SmartphoneMapper.class).toDto(s))
                    .collect(Collectors.toList());
            dto.setSmartphones(smartphones);
        } else {
            dto.setSmartphones(new ArrayList<>());
        }
    }
}
