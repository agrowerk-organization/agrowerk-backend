package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.InputCategoryRepository;

@Service
public class InputCategoryService {
    private final InputCategoryRepository inputCategoryRepository;

    public InputCategoryService(InputCategoryRepository inputCategoryRepository) {
        this.inputCategoryRepository = inputCategoryRepository;
    }
}
