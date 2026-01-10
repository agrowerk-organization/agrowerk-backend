package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.SeasonRepository;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }
}
