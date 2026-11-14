package Learning.Management.System.repository;

import Learning.Management.System.entity.User;
import Learning.Management.System.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    boolean existsByRole(Learning.Management.System.entity.Role role);
}
