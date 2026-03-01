package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.views.FieldProductivityView;

import java.util.UUID;

@Repository
public interface FieldProductivityViewRepository extends JpaRepository<FieldProductivityView, UUID> {

    FieldProductivityView findByFieldId(UUID fieldId);

}
