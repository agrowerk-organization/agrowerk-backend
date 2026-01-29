package tech.agrowerk.application.controller.farming;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.farming.HarvestService;

@RestController
@RequestMapping("/harvests")
public class HarvestController {
    private HarvestService harvestService;

    public HarvestController(HarvestService harvestService) {
        this.harvestService = harvestService;
    }
}
