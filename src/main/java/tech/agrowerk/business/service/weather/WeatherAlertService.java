package tech.agrowerk.business.service.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.weather.Alert;
import tech.agrowerk.business.mapper.WeatherMapper;
import tech.agrowerk.application.dto.weather.RealTimeUpdate;
import tech.agrowerk.infrastructure.exception.local.WeatherAlertException;
import tech.agrowerk.infrastructure.model.weather.WeatherAlert;
import tech.agrowerk.infrastructure.model.weather.WeatherCurrent;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertSeverity;
import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertType;
import tech.agrowerk.infrastructure.repository.weather.WeatherAlertRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherAlertService {

    private final WeatherAlertRepository alertRepository;
    private final WeatherMapper weatherMapper;
    private final ApplicationEventPublisher eventPublisher;

    private static final Map<WeatherAlertType, AlertThreshold> ALERT_THRESHOLDS = Map.of(
            WeatherAlertType.FROST, new AlertThreshold(BigDecimal.valueOf(3.0), WeatherAlertSeverity.CRITICAL),
            WeatherAlertType.HEAT_WAVE, new AlertThreshold(BigDecimal.valueOf(35.0), WeatherAlertSeverity.HIGH),
            WeatherAlertType.HEAVY_RAIN, new AlertThreshold(BigDecimal.valueOf(50.0), WeatherAlertSeverity.HIGH),
            WeatherAlertType.STRONG_WINDS, new AlertThreshold(BigDecimal.valueOf(15.0), WeatherAlertSeverity.MEDIUM)
    );

    private static final int HIGH_HUMIDITY_THRESHOLD = 85;
    private static final int ALERT_DEDUPLICATION_HOURS = 6;
    private static final int ALERT_VALIDITY_HOURS = 24;
    private static final int OLD_ALERTS_RETENTION_DAYS = 30;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "activeAlerts", key = "#current.location.id", allEntries = true, cacheManager = "caffeineCacheManager"),
            @CacheEvict(value = "activeAlerts", key = "#current.location.id", allEntries = true, cacheManager = "redisCacheManager")
    })
    public List<WeatherAlert> processWeatherDataForAlerts(WeatherCurrent current) {
        log.debug("Processing weather data for alerts: location={}, temp={}, humidity={}",
                current.getLocation().getName(), current.getTemperature(), current.getHumidity());

        List<WeatherAlert> generatedAlerts = new ArrayList<>();

        try {
            processTemperatureAlerts(current, generatedAlerts);
            processPrecipitationAlerts(current, generatedAlerts);
            processWindAlerts(current, generatedAlerts);
            processDiseaseRiskAlerts(current, generatedAlerts);

            log.info("Weather alert processing completed: location={}, alerts_generated={}",
                    current.getLocation().getName(), generatedAlerts.size());

        } catch (Exception e) {
            log.error("Error processing weather alerts for location: {}",
                    current.getLocation().getName(), e);
        }

        return generatedAlerts;
    }

    private void processTemperatureAlerts(WeatherCurrent current, List<WeatherAlert> alerts) {
        if (current.getTemperature() == null) return;

        BigDecimal temp = current.getTemperature();

        if (temp.compareTo(ALERT_THRESHOLDS.get(WeatherAlertType.FROST).threshold) <= 0) {
            createAlertIfNeeded(
                    current,
                    WeatherAlertType.FROST,
                    WeatherAlertSeverity.CRITICAL,
                    "Risco Crítico de Geada",
                    String.format("Temperatura de %.1f°C pode causar formação de gelo e danos severos às plantas. " +
                            "Culturas sensíveis estão em alto risco.", temp),
                    buildRecommendations(
                            "Cobrir imediatamente plantas sensíveis com mantas térmicas",
                            "Irrigar antes do anoitecer para aumentar capacidade térmica do solo",
                            "Ativar sistemas de aquecimento/ventilação em estufas",
                            "Monitorar temperaturas durante toda a noite"
                    )
            ).ifPresent(alerts::add);
        }

        if (temp.compareTo(ALERT_THRESHOLDS.get(WeatherAlertType.HEAT_WAVE).threshold) >= 0) {
            createAlertIfNeeded(
                    current,
                    WeatherAlertType.HEAT_WAVE,
                    WeatherAlertSeverity.HIGH,
                    "Alerta de Onda de Calor",
                    String.format("Temperatura de %.1f°C pode causar estresse térmico severo nas culturas. " +
                            "Risco de redução de produtividade e qualidade.", temp),
                    buildRecommendations(
                            "Aumentar frequência de irrigação em 30-50%",
                            "Irrigar preferencialmente nas horas mais frescas (manhã/noite)",
                            "Aplicar sombreamento em culturas sensíveis",
                            "Monitorar sinais de murcha e estresse hídrico"
                    )
            ).ifPresent(alerts::add);
        }
    }

    private void processPrecipitationAlerts(WeatherCurrent current, List<WeatherAlert> alerts) {
        if (current.getRainfall() == null) return;

        BigDecimal rainfall = current.getRainfall();

        if (rainfall.compareTo(ALERT_THRESHOLDS.get(WeatherAlertType.HEAVY_RAIN).threshold) >= 0) {
            WeatherAlertSeverity severity = rainfall.compareTo(BigDecimal.valueOf(80)) >= 0
                    ? WeatherAlertSeverity.CRITICAL
                    : WeatherAlertSeverity.HIGH;

            createAlertIfNeeded(
                    current,
                    WeatherAlertType.HEAVY_RAIN,
                    severity,
                    "Chuva Intensa Detectada",
                    String.format("Precipitação acumulada de %.1fmm. Alto risco de alagamento, erosão e " +
                            "compactação do solo.", rainfall),
                    buildRecommendations(
                            "Verificar e desobstruir sistemas de drenagem",
                            "Proteger áreas com declive contra erosão",
                            "Evitar tráfego de maquinário no solo saturado",
                            "Monitorar níveis de água em áreas baixas",
                            "Adiar aplicações de defensivos agrícolas"
                    )
            ).ifPresent(alerts::add);
        }
    }

    private void processWindAlerts(WeatherCurrent current, List<WeatherAlert> alerts) {
        if (current.getWindSpeed() == null) return;

        BigDecimal windSpeed = current.getWindSpeed();

        if (windSpeed.compareTo(ALERT_THRESHOLDS.get(WeatherAlertType.STRONG_WINDS).threshold) >= 0) {
            WeatherAlertSeverity severity = windSpeed.compareTo(BigDecimal.valueOf(20)) >= 0
                    ? WeatherAlertSeverity.HIGH
                    : WeatherAlertSeverity.MEDIUM;

            createAlertIfNeeded(
                    current,
                    WeatherAlertType.STRONG_WINDS,
                    severity,
                    "Ventos Fortes Previstos",
                    String.format("Ventos de %.1f m/s (%.0f km/h) podem causar danos mecânicos às culturas, " +
                            "estruturas e sistemas de irrigação.", windSpeed, windSpeed.multiply(BigDecimal.valueOf(3.6))),
                    buildRecommendations(
                            "Inspecionar e reforçar estruturas (estufas, telas, tutores)",
                            "Verificar sistemas de irrigação e fertirrigação",
                            "Desligar sistemas de pulverização",
                            "Proteger ou remover equipamentos leves",
                            "Verificar quebra-ventos e cercas"
                    )
            ).ifPresent(alerts::add);
        }
    }


    private void processDiseaseRiskAlerts(WeatherCurrent current, List<WeatherAlert> alerts) {
        if (current.getHumidity() == null || current.getTemperature() == null) return;

        Integer humidity = current.getHumidity();
        BigDecimal temp = current.getTemperature();

        if (humidity >= HIGH_HUMIDITY_THRESHOLD &&
                temp.compareTo(BigDecimal.valueOf(15)) >= 0 &&
                temp.compareTo(BigDecimal.valueOf(30)) <= 0) {

            WeatherAlertSeverity severity = humidity >= 90
                    ? WeatherAlertSeverity.HIGH
                    : WeatherAlertSeverity.MEDIUM;

            createAlertIfNeeded(
                    current,
                    WeatherAlertType.DISEASE_FAVORABLE,
                    severity,
                    "Condições Favoráveis a Doenças Fúngicas",
                    String.format("Alta umidade relativa (%d%%) combinada com temperatura de %.1f°C " +
                            "cria ambiente ideal para desenvolvimento de fungos fitopatogênicos (ferrugem, míldio, " +
                            "antracnose, oídio).", humidity, temp),
                    buildRecommendations(
                            "Intensificar monitoramento fitossanitário diário",
                            "Avaliar aplicação preventiva de fungicidas conforme calendário",
                            "Melhorar ventilação em ambientes protegidos",
                            "Evitar irrigação por aspersão no final da tarde",
                            "Registrar sintomas iniciais para diagnóstico precoce"
                    )
            ).ifPresent(alerts::add);
        }
    }

    private Optional<WeatherAlert> createAlertIfNeeded(
            WeatherCurrent current,
            WeatherAlertType type,
            WeatherAlertSeverity severity,
            String title,
            String description,
            String recommendedActions) {

        if (isDuplicateAlert(current.getLocation(), type)) {
            log.debug("Skipping duplicate alert: type={}, location={}", type, current.getLocation().getName());
            return Optional.empty();
        }

        WeatherAlert alert = WeatherAlert.builder()
                .location(current.getLocation())
                .alertType(type)
                .severity(severity)
                .title(title)
                .description(description)
                .recommendedActions(recommendedActions)
                .startTime(current.getTimestamp())
                .endTime(current.getTimestamp().plus(ALERT_VALIDITY_HOURS, ChronoUnit.HOURS))
                .isActive(true)
                .notified(false)
                .triggerCondition(buildTriggerCondition(current, type))
                .source("AUTOMATED_SYSTEM")
                .build();

        alert = alertRepository.save(alert);

        log.info("Created weather alert: id={}, type={}, severity={}, location={}",
                alert.getId(), type, severity, current.getLocation().getName());

        processAlertNotificationsAsync(alert);

        return Optional.of(alert);
    }

    private boolean isDuplicateAlert(WeatherLocation location, WeatherAlertType type) {
        Instant cutoffTime = Instant.now().minus(ALERT_DEDUPLICATION_HOURS, ChronoUnit.HOURS);

        return alertRepository.findByLocationAndIsActiveTrue(location).stream()
                .anyMatch(alert ->
                        alert.getAlertType() == type &&
                                alert.getStartTime().isAfter(Instant.from(cutoffTime))
                );
    }

    @Async
    protected void processAlertNotificationsAsync(WeatherAlert alert) {
        CompletableFuture.runAsync(() -> {
            try {
                sendWebSocketNotification(alert);

                alert.setNotified(true);
                alert.setNotifiedAt(Instant.now());
                alertRepository.save(alert);

            } catch (Exception e) {
                log.error("Failed to process alert notifications: alertId={}", alert.getId(), e);
            }
        });
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


    @Cacheable(value = "activeAlerts", key = "#location.id", cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    public List<Alert> getActiveAlertsByLocation(WeatherLocation location) {
        return alertRepository.findByLocationAndIsActiveTrue(location).stream()
                .map(weatherMapper::toAlertDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WeatherAlert> getPendingNotifications() {
        return alertRepository.findByIsActiveTrueAndNotifiedFalse();
    }


    @Transactional(readOnly = true)
    public Map<String, Object> getAlertStatistics(WeatherLocation location) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAlerts", alertRepository.countByLocation(location));

        Map<WeatherAlertSeverity, Long> bySeverity = Arrays.stream(WeatherAlertSeverity.values())
                .collect(Collectors.toMap(
                        severity -> severity,
                        severity -> alertRepository.countByLocationAndSeverity(location, severity)
                ));
        stats.put("bySeverity", bySeverity);

        return stats;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "activeAlerts", key = "#alertId", allEntries = true, cacheManager = "caffeineCacheManager"),
            @CacheEvict(value = "activeAlerts", key = "#alertId", allEntries = true, cacheManager = "redisCacheManager")
    })
    public void resolveAlert(UUID alertId, String resolvedBy) {
        WeatherAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new WeatherAlertException("Alert not found: " + alertId));

        alert.setIsActive(false);
        alert.setEndTime(Instant.now());
        alertRepository.save(alert);

        sendResolutionNotification(alert);

        log.info("Alert manually resolved: id={}, by={}", alertId, resolvedBy);
    }


    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deactivateExpiredAlerts() {
        log.info("Starting expired alerts deactivation job");

        Instant now = Instant.now();

        List<WeatherAlert> expiredAlerts = alertRepository.findByIsActiveTrueAndEndTimeBefore(now);

        for (WeatherAlert alert : expiredAlerts) {
            alert.setIsActive(false);
            alertRepository.save(alert);
            sendResolutionNotification(alert);
        }

        log.info("Expired alerts deactivation completed: count={}", expiredAlerts.size());
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldAlerts() {
        log.info("Starting old alerts cleanup job");

        Instant cutoffDate = Instant.now().minus(OLD_ALERTS_RETENTION_DAYS, ChronoUnit.DAYS);

        try {
            alertRepository.deleteByIsActiveFalseAndEndTimeBefore(cutoffDate);
            log.info("Old alerts cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during old alerts cleanup", e);
        }
    }


    @Scheduled(fixedDelay = 900000)
    @Transactional
    public void reprocessPendingNotifications() {
        List<WeatherAlert> pending = getPendingNotifications();

        if (!pending.isEmpty()) {
            log.info("Reprocessing pending notifications: count={}", pending.size());
            pending.forEach(this::processAlertNotificationsAsync);
        }
    }

    private void sendResolutionNotification(WeatherAlert alert) {
        try {
            RealTimeUpdate update = RealTimeUpdate.builder()
                    .eventType("ALERT_RESOLVED")
                    .locationId(alert.getLocation().getId())
                    .alert(weatherMapper.toAlertDTO(alert))
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to send resolution notification: alertId={}", alert.getId(), e);
        }
    }

    private String buildRecommendations(String... actions) {
        return Arrays.stream(actions)
                .map(action -> "• " + action)
                .collect(Collectors.joining("\n"));
    }

    private String buildTriggerCondition(WeatherCurrent current, WeatherAlertType type) {
        return switch (type) {
            case FROST -> String.format("Temperature %.1f°C ≤ %.1f°C",
                    current.getTemperature(), ALERT_THRESHOLDS.get(type).threshold);
            case HEAT_WAVE -> String.format("Temperature %.1f°C ≥ %.1f°C",
                    current.getTemperature(), ALERT_THRESHOLDS.get(type).threshold);
            case HEAVY_RAIN -> String.format("Rainfall %.1fmm ≥ %.1fmm",
                    current.getRainfall(), ALERT_THRESHOLDS.get(type).threshold);
            case STRONG_WINDS -> String.format("Wind speed %.1f m/s ≥ %.1f m/s",
                    current.getWindSpeed(), ALERT_THRESHOLDS.get(type).threshold);
            case DISEASE_FAVORABLE -> String.format("Humidity %d%% ≥ %d%% AND Temp %.1f°C in range",
                    current.getHumidity(), HIGH_HUMIDITY_THRESHOLD, current.getTemperature());
            default -> type.name() + " threshold exceeded";
        };
    }

    private record AlertThreshold(BigDecimal threshold, WeatherAlertSeverity severity) {}
}