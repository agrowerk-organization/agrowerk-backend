package tech.agrowerk.business.service.support;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.cache.CachedPage;
import tech.agrowerk.application.dto.request.support.FaqRequest;
import tech.agrowerk.application.dto.response.support.FaqResponse;
import tech.agrowerk.business.mapper.support.FaqMapper;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.support.Faq;
import tech.agrowerk.infrastructure.model.support.enums.FaqCategory;
import tech.agrowerk.infrastructure.repository.support.FaqRepository;

import java.util.UUID;

@Service
public class FaqService {
    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    public FaqService(FaqRepository faqRepository, FaqMapper faqMapper) {
        this.faqRepository = faqRepository;
        this.faqMapper = faqMapper;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "faqs", key = "(#faqCategory != null ? #faqCategory.name() : 'all') + '_p' + #pageable.pageNumber + '_s' + #pageable.pageSize")
    public CachedPage<FaqResponse> listActive(FaqCategory faqCategory, Pageable pageable) {
        Page<FaqResponse> page = faqCategory != null
                ? faqRepository.findByIsActiveTrueAndFaqCategoryOrderByDisplayOrderAsc(faqCategory, pageable)
                .map(faqMapper::toResponse)
                : faqRepository.findByIsActiveTrueOrderByFaqCategoryAscDisplayOrderAsc(pageable)
                .map(faqMapper::toResponse);
        return CachedPage.from(page);
    }

    @Transactional
    public FaqResponse getAndCount(UUID faqId) {
        Faq faq = faqRepository.findById(faqId)
                .filter(Faq::isActive)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found"));

        faq.setViewCount(faq.getViewCount() + 1);
        return faqMapper.toResponse(faqRepository.save(faq));
    }

    @Transactional
    @CacheEvict(value = "faqs", allEntries = true)
    public FaqResponse createFaq(FaqRequest request) {
        Faq faq = faqMapper.toEntity(request);
        return faqMapper.toResponse(faqRepository.save(faq));
    }

    @Transactional
    @CacheEvict(value = "faqs", allEntries = true)
    public FaqResponse updateFaq(UUID faqId, FaqRequest request) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
        faq.setQuestion(request.question());
        faq.setAnswer(request.answer());
        faq.setFaqCategory(request.faqCategory());
        faq.setDisplayOrder(request.displayOrder());
        return faqMapper.toResponse(faqRepository.save(faq));
    }

    @Transactional
    @CacheEvict(value = "faqs", allEntries = true)
    public void deactivate(UUID id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found"));
        faq.setActive(false);
        faqRepository.save(faq);
    }
}
