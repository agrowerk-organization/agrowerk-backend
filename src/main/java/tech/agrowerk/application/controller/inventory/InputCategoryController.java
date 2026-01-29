package tech.agrowerk.application.controller.inventory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.business.service.inventory.InputCategoryService;

@RestController
@RequestMapping("/input-categories")
public class InputCategoryController {
    private final InputCategoryService inputCategoryService;

    public InputCategoryController(InputCategoryService inputCategoryService) {
        this.inputCategoryService = inputCategoryService;
    }
}
