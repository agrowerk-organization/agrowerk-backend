package tech.agrowerk.business.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tech.agrowerk.business.listener.events.PropertyUpdatedEvent;
import tech.agrowerk.business.service.weather.WeatherLocationService;

@Component
@Slf4j
public class WeatherLocationEventListener {

    private final WeatherLocationService weatherLocationService;

    public WeatherLocationEventListener(WeatherLocationService weatherLocationService) {
        this.weatherLocationService = weatherLocationService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPropertyUpdated(PropertyUpdatedEvent event) {
        log.debug("Syncing weather location after property update: {}", event.propertyId());
        weatherLocationService.syncFromPropertyUpdate(event);
    }
}