package tech.agrowerk.application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.PlantingInputService;

@RestController
@RequestMapping("/planning-inputs")
public class PlantingInputController {

    private final PlantingInputService plantingInputService;

    public PlantingInputController(PlantingInputService plantingInputService) {
        this.plantingInputService = plantingInputService;
    }
}
