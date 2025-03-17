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
