package app.mapper;

import app.dto.UserDTO;
import app.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<User, UserDTO> {

    @Mapping(target = "password", ignore = true)
    @Override
    User merge(@MappingTarget User entity, UserDTO dto);
}
//package app.mapper;
//
//import app.dto.UserDTO;
//import app.models.User;
//import java.util.List;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.Mappings;
//import org.mapstruct.MappingTarget;
//
//@Mapper(componentModel = "spring")
//public interface UserMapper {
//
//    @Mappings({
//            @Mapping(target = "id", ignore = true),
//            @Mapping(target = "username", source = "username"),
//            @Mapping(target = "password", source = "password"),
//            @Mapping(target = "role", ignore = true),
//            @Mapping(target = "smartphones", ignore = true),
//            @Mapping(target = "authorities", ignore = true)
//    })
//    User toEntity(UserDTO dto);
//
//    @Mappings({
//            @Mapping(target = "smartphoneIds", expression = "java(user.getSmartphones() == null ? new java.util.ArrayList<>() : user.getSmartphones().stream().map(s -> s.getId()).collect(java.util.stream.Collectors.toList()))")
//    })
//    UserDTO toDto(User user);
//
//    @Mappings({
//            @Mapping(target = "id", ignore = true),
//            @Mapping(target = "username", source = "dto.username"),
//            @Mapping(target = "password", ignore = true),
//            @Mapping(target = "role", ignore = true),
//            @Mapping(target = "smartphones", ignore = true),
//            @Mapping(target = "authorities", ignore = true)
//    })
//    User merge(User existingUser, UserDTO dto);
//
//    // Добавляем явное определение для списка
//    List<UserDTO> toDtos(Iterable<User> users);
//}
//

