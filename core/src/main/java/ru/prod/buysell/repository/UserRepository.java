package ru.prod.buysell.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prod.buysell.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmail(String email);
}
