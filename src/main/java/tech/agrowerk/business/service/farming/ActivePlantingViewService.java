package tech.agrowerk.business.service.farming;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.views.ActivePlantingResponse;
import tech.agrowerk.business.mapper.FarmingViewMapper;
import tech.agrowerk.infrastructure.model.farming.views.ActivePlantingView;
import tech.agrowerk.infrastructure.repository.farming.ActivePlantingViewRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ActivePlantingViewService {

   private final ActivePlantingViewRepository activePlantingViewRepository;
   private final FarmingViewMapper farmingViewMapper;

    public ActivePlantingViewService(ActivePlantingViewRepository activePlantingViewRepository, FarmingViewMapper farmingViewMapper) {
        this.activePlantingViewRepository = activePlantingViewRepository;
        this.farmingViewMapper = farmingViewMapper;
    }

    public Optional<ActivePlantingResponse> findActivePlantingByPlantingId(UUID plantingId) {
        return Optional.ofNullable(activePlantingViewRepository.findByPlantingId(plantingId))
                .map(farmingViewMapper::toActivePlantingResponse);
    }
}