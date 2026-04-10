package tech.agrowerk.business.service.barter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.barter.BarterExchangeRate;
import tech.agrowerk.infrastructure.repository.barter.BarterExchangeRateRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BarterExchangeRateService {

    private final BarterExchangeRateRepository barterExchangeRateRepository;

    public BarterExchangeRateService(BarterExchangeRateRepository barterExchangeRateRepository) {
        this.barterExchangeRateRepository = barterExchangeRateRepository;
    }

    @Transactional(readOnly = true)
    public List<BarterExchangeRate> listByCrop() {
        return barterExchangeRateRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<BarterExchangeRate> listByCrop(UUID cropId) {
        return barterExchangeRateRepository.findByCrop_IdAndActiveTrue(cropId);
    }

    @Transactional
    public BarterExchangeRate create(BarterExchangeRate rate) {
        BarterExchangeRate saved = barterExchangeRateRepository.save(rate);
        log.info("BarterExchangeRate created id={}", saved.getId());
        return saved;
    }

    @Transactional
    public void deactivate(UUID id) {
        BarterExchangeRate rate = barterExchangeRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exchange rate not found"));
        rate.setActive(false);
        log.info("BarterExchangeRate deactivated id={}", id);
    }
}
