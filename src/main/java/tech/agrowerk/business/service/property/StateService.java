package tech.agrowerk.business.service.property;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.create.CreateStateRequest;
import tech.agrowerk.application.dto.request.update.UpdateStateRequest;
import tech.agrowerk.application.dto.response.StateResponse;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.property.State;
import tech.agrowerk.infrastructure.repository.property.StateRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class StateService {

    private final StateRepository stateRepository;
    private final AuthUtil authUtil;

    public StateService(StateRepository stateRepository, AuthUtil authUtil) {
        this.stateRepository = stateRepository;
        this.authUtil = authUtil;
    }

    @Transactional
    @CacheEvict(value = "states", allEntries = true)
    public StateResponse createState(CreateStateRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        if (stateRepository.findByCode(request.code()).isPresent()) {
            throw new EntityAlreadyExistsException("State already registered");
        }

        State newState = new State();
        newState.setCode(request.code());
        newState.setName(request.name());

        State saved = stateRepository.save(newState);

        return new StateResponse(saved.getId(), saved.getCode(), saved.getName());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "states", key = "#stateId")
    public StateResponse findStateById(UUID stateId) {
        State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new EntityNotFoundException("State not found"));

        return new StateResponse(state.getId(), state.getCode(), state.getName());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "states", key = "{ #searchTerm, #pageable.pageNumber, #pageable.pageSize }")
    public Page<StateResponse> searchStates(String searchTerm, Pageable pageable) {
        return stateRepository.searchStates(searchTerm, pageable)
                .map(state -> new StateResponse(state.getId(), state.getCode(), state.getName()));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "states", key = "'all'")
    public List<StateResponse> listAllStates() {
        return stateRepository.findAll()
                .stream()
                .map(StateResponse::new)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "states", key = "#stateId"),
            @CacheEvict(value = "states", allEntries = true)
    })
    public StateResponse updateState(UUID stateId, UpdateStateRequest request) {
        State state = stateRepository.findById(stateId)
                .orElseThrow(() -> new EntityNotFoundException("State not found"));

        boolean hasChanges = false;

        if (request.code() != null) {
            state.setCode(request.code());
            hasChanges = true;
        }

        if (request.name() != null) {
            state.setName(request.name());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for state id={}", stateId);
        }

        log.info("State updated id={}", stateId);

        return new StateResponse(state.getId(), state.getCode(), state.getName());
    }
}
