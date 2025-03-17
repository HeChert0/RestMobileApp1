package app.mapper;

import app.dto.UserDto;
import app.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<User, UserDto> {

    @Mapping(target = "password", ignore = true)
    @Override
    User merge(@MappingTarget User entity, UserDto dto);
}


