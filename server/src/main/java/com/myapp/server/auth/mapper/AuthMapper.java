package com.myapp.server.auth.mapper;

import com.myapp.server.auth.dto.UserResponse;
import com.myapp.server.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName());
    }
}
