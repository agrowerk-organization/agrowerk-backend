package tech.agrowerk.infrastructure.repository.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.property.State;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {
    Optional<State> findByCode(String code);

    @Query("SELECT s FROM State s WHERE " +
            "lower(s.name) LIKE lower(concat('%', :searchTerm, '%')) OR " +
            "lower(s.code) LIKE lower(concat('%', :searchTerm, '%'))")
    Page<State> searchStates(@Param("searchTerm") String searchTerm, Pageable pageable);
}
