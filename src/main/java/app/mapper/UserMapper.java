package app.mapper;

import app.dto.OrderDto;
import app.dto.SmartphoneDto;
import app.dto.UserDto;
import app.models.Order;
import app.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;


@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<User, UserDto> {

    @SuppressWarnings("checkstyle:Indentation")
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "username", source = "username"),
            @Mapping(target = "password", source = "password"),
            @Mapping(target = "role", ignore = true),
            @Mapping(target = "orders", ignore = true)
    })
    @Override
    User toEntity(UserDto dto);

    @Mappings({@Mapping(target = "orderIds",
            expression = "java(user.getOrders() == null ?"
                    + " new java.util.ArrayList<>() : "
                    + "user.getOrders().stream().map(o ->"
                    + " o.getId()).collect(java.util.stream.Collectors.toList()))"),
               @Mapping(target = "orders", ignore = true)
    })
    @Override
    UserDto toDto(User user);

    @AfterMapping
    default void fillOrders(@MappingTarget UserDto dto, User user) {

        if (user.getOrders() != null) {
            List<OrderDto> orders = user.getOrders().stream()
                    .map(o -> org.mapstruct.factory.Mappers
                            .getMapper(OrderMapper.class).toDto(o))
                    .collect(Collectors.toList());
            dto.setOrders(orders);
        } else {
            dto.setOrders(new ArrayList<>());
        }
    }
}