package tech.agrowerk.business.service.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.weather.RealTimeUpdate;
import tech.agrowerk.business.mapper.weather.WeatherMapper;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.weather.WeatherAlert;
import tech.agrowerk.infrastructure.repository.weather.WeatherAlertRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class WeatherAlertNotificationDelegate {

    private final WeatherAlertRepository alertRepository;
    private final WeatherMapper weatherMapper;
    private final ApplicationEventPublisher eventPublisher;

    public WeatherAlertNotificationDelegate(WeatherAlertRepository alertRepository,
                                            WeatherMapper weatherMapper,
                                            ApplicationEventPublisher eventPublisher) {
        this.alertRepository = alertRepository;
        this.weatherMapper = weatherMapper;
        this.eventPublisher = eventPublisher;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> processAlertNotificationsAsync(WeatherAlert alert) {
        try {
            sendWebSocketNotification(alert);
            markAsNotified(alert.getId());
        } catch (Exception e) {
            log.error("Failed to process alert notifications: alertId={}", alert.getId(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void markAsNotified(UUID alertId) {
        WeatherAlert alert = alertRepository.findByIdWithLock(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found"));

        if (!alert.getNotified()) {
            alert.setNotified(true);
            alert.setNotifiedAt(Instant.now());
            alertRepository.save(alert);
            log.debug("Alert {} marked as notified", alertId);
        }
    }

    private void sendWebSocketNotification(WeatherAlert alert) {
        try {
            RealTimeUpdate update = RealTimeUpdate.builder()
                    .eventType("ALERT_CREATED")
                    .locationId(alert.getLocation().getId())
                    .alert(weatherMapper.toAlertDTO(alert))
                    .timestamp(LocalDateTime.now())
                    .build();


            log.debug("WebSocket notification sent: alertId={}, locationId={}",
                    alert.getId(), alert.getLocation().getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification: alertId={}", alert.getId(), e);
        }
    }
}