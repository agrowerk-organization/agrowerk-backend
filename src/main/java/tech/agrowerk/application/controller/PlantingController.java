package tech.agrowerk.application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.PlantingService;

@RestController
@RequestMapping("/plantings")
public class PlantingController {

    private final PlantingService plantingService;

    public PlantingController(PlantingService plantingService) {
        this.plantingService = plantingService;
    }
}
