package pointer.Pointer_Spring.User.repository;

import pointer.Pointer_Spring.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndStatus(String email, int status);
    Optional<User> findByIdAndStatus(String id, int status);
    Optional<User> findByTokenAndStatus(String token, int status);
    boolean existsByEmailAndStatus(String email, int status);
    Optional<User> findById(String id);

}
