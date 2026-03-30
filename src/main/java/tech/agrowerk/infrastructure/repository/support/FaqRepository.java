package tech.agrowerk.infrastructure.repository.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.support.Faq;
import tech.agrowerk.infrastructure.model.support.enums.FaqCategory;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqRepository extends JpaRepository<Faq, UUID> {

    Page<Faq> findByIsActiveTrueOrderByFaqCategoryAscDisplayOrderAsc(Pageable pageable);

    Page<Faq> findByIsActiveTrueAndFaqCategoryOrderByDisplayOrderAsc(FaqCategory faqCategory, Pageable pageable);

}
