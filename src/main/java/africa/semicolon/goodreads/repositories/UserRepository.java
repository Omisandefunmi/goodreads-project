package africa.semicolon.goodreads.repositories;

import africa.semicolon.goodreads.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByFirstName(String firstName);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserById(long id);
}
