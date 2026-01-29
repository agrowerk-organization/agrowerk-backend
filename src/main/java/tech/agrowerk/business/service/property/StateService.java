package tech.agrowerk.business.service.property;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.property.StateRepository;

@Service
public class StateService {

    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }
}
