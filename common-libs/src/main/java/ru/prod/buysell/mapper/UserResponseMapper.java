package ru.prod.buysell.mapper;

import org.mapstruct.Mapper;
import ru.prod.buysell.dto.UserResponse;
import ru.prod.buysell.entity.User;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface UserResponseMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}