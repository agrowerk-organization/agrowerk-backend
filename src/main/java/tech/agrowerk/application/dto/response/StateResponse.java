package tech.agrowerk.application.dto.response;

import tech.agrowerk.infrastructure.model.property.State;

import java.util.UUID;

public record StateResponse(UUID id, String code, String name) {
    public StateResponse(State state) {
        this(state.getId(), state.getCode(), state.getName());
    }
}