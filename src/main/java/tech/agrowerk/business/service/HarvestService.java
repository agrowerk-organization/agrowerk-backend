package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.HarvestRepository;

@Service
public class HarvestService {
    private final HarvestRepository harvestRepository;

    public HarvestService(HarvestRepository harvestRepository) {
        this.harvestRepository = harvestRepository;
    }
}
