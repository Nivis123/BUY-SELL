package ru.prod.buysell.mapper;

import org.mapstruct.Mapper;
import ru.prod.buysell.dto.UserRegistrationRequest;
import ru.prod.buysell.entity.User;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    public User toEntity(UserRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());

        return user;
    }
}