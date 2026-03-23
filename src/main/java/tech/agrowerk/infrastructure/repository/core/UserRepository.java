package tech.agrowerk.infrastructure.repository.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.core.enums.RoleType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByCpfAndIdNot(String cpf, UUID id);

    Page<User> findByRole_NameAndNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            RoleType role, String name, String email, Pageable pageable);
}
