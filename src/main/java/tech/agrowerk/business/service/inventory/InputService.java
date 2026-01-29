package tech.agrowerk.business.service.inventory;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;

@Service
public class InputService {
    private final InputRepository inputRepository;

    public InputService(InputRepository inputRepository) {
        this.inputRepository = inputRepository;
    }
}
