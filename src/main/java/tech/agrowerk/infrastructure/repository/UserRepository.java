package tech.agrowerk.infrastructure.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByTelephone(String telephone);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
