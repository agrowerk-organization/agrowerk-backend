package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.views.FieldProductivityResponse;
import tech.agrowerk.business.mapper.FarmingViewMapper;
import tech.agrowerk.infrastructure.model.farming.views.FieldProductivityView;
import tech.agrowerk.infrastructure.repository.farming.FieldProductivityViewRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class FieldProductivityViewService {

    private final FieldProductivityViewRepository fieldProductivityViewRepository;
    private final FarmingViewMapper farmingViewMapper;

    public FieldProductivityViewService(FieldProductivityViewRepository fieldProductivityViewRepository, FarmingViewMapper farmingViewMapper) {
        this.fieldProductivityViewRepository = fieldProductivityViewRepository;
        this.farmingViewMapper = farmingViewMapper;
    }

    @Cacheable(value = "fieldProductivity", key = "#fieldId",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    public Optional<FieldProductivityResponse> findFieldProductivityViewById(UUID fieldId) {
        return fieldProductivityViewRepository.findByFieldId(fieldId)
                .map(farmingViewMapper::toFieldProductivityResponse);
    }
}
