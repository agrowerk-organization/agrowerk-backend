package tech.agrowerk.infrastructure.repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.core.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByCpfAndIdNot(String cpf, UUID id);


}
