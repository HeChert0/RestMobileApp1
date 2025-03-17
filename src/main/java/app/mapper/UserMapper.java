package app.mapper;

import app.dto.UserDto;
import app.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
                    + " o.getId()).collect(java.util.stream.Collectors.toList()))")
    })
    @Override
    UserDto toDto(User user);
}