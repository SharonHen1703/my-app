package com.myapp.server.users;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;

    @Transactional
    public User create(UserCreateRequest req) {
        if (repo.existsByEmailIgnoreCase(req.email())) {
            throw new DataIntegrityViolationException("email already exists");
        }
        User user = User.builder()
                .email(req.email())
                .firstName(req.firstName())
                .lastName(req.lastName())
                .build();
        return repo.save(user);
    }
}
