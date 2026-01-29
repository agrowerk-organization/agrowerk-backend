package tech.agrowerk.business.service.farming;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.farming.HarvestRepository;

@Service
public class HarvestService {
    private final HarvestRepository harvestRepository;

    public HarvestService(HarvestRepository harvestRepository) {
        this.harvestRepository = harvestRepository;
    }
}
