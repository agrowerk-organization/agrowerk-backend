package tech.agrowerk.business.service.farming;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.views.ActivePlantingResponse;
import tech.agrowerk.business.mapper.farming.FarmingViewMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.repository.farming.ActivePlantingViewRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ActivePlantingViewService {

   private final ActivePlantingViewRepository activePlantingViewRepository;
   private final FarmingViewMapper farmingViewMapper;
   private final OwnershipValidator ownershipValidator;
   private final AuthUtil authUtil;

    public ActivePlantingViewService(ActivePlantingViewRepository activePlantingViewRepository, FarmingViewMapper farmingViewMapper, OwnershipValidator ownershipValidator, AuthUtil authUtil) {
        this.activePlantingViewRepository = activePlantingViewRepository;
        this.farmingViewMapper = farmingViewMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }


    public Optional<ActivePlantingResponse> findActivePlantingByPlantingId(UUID plantingId) {
        return activePlantingViewRepository.findByPlantingId(plantingId)
                .map(view -> {
                    ownershipValidator.validateOwnership(
                            view.getPropertyId(), authUtil.getAuthenticatedUser().id());
                    return farmingViewMapper.toActivePlantingResponse(view);
                });
    }

    public List<ActivePlantingResponse> findByProperty(UUID propertyId) {
        ownershipValidator.validateOwnership(propertyId, authUtil.getAuthenticatedUser().id());

        return activePlantingViewRepository.findAllByPropertyId(propertyId)
                .stream()
                .map(farmingViewMapper::toActivePlantingResponse)
                .toList();
    }
}