package tech.agrowerk.application.controller.weather;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.agrowerk.application.controller.base.BaseControllerTest;
import tech.agrowerk.application.dto.weather.Alert;
import tech.agrowerk.business.service.weather.WeatherAlertService;
import tech.agrowerk.infrastructure.model.weather.WeatherAlert;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;
import tech.agrowerk.infrastructure.security.services.RateLimitService;
import tech.agrowerk.infrastructure.security.services.TokenBlacklistService;
import tech.agrowerk.infrastructure.security.validator.JwtUserValidator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherAlertController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherAlertControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherAlertService alertService;

    @MockitoBean
    private WeatherLocationRepository locationRepository;

    private static final UUID LOCATION_ID = UUID.randomUUID();
    private static final UUID ALERT_ID    = UUID.randomUUID();

    @Override
    protected void setUp() {
        super.setUpSecurity();
    }

    private WeatherLocation buildLocation() {
        WeatherLocation loc = new WeatherLocation();
        loc.setId(LOCATION_ID);
        loc.setName("Horto - Juazeiro do Norte");
        return loc;
    }


    @Test
    @Order(1)
    @DisplayName("1. GET /weather/alerts/location/{id} - 200 OK for PRODUCER with alerts")
    @WithMockUser(authorities = "PRODUCER")
    void testGetActiveAlertsByLocation_Returns200() throws Exception {
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(buildLocation()));
        when(alertService.getActiveAlertsByLocation(any())).thenReturn(List.of(Alert.builder().build()));

        mockMvc.perform(get("/weather/alerts/location/{id}", LOCATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /weather/alerts/location/{id} - 400 when location not found")
    @WithMockUser(authorities = "PRODUCER")
    void testGetActiveAlertsByLocation_LocationNotFound_Returns400() throws Exception {
        when(locationRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/weather/alerts/location/{id}", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /weather/alerts/location/{id} - 401 when unauthenticated")
    void testGetActiveAlertsByLocation_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/weather/alerts/location/{id}", LOCATION_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("4. GET /weather/alerts/location/{id} - 403 for unauthorized role")
    @WithMockUser(authorities = "UNKNOWN_ROLE")
    void testGetActiveAlertsByLocation_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(get("/weather/alerts/location/{id}", LOCATION_ID))
                .andExpect(status().isForbidden());
    }


    @Test
    @Order(5)
    @DisplayName("5. GET /weather/alerts/pending - 200 OK for SYSTEM_ADMIN")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testGetPendingNotifications_AsSystemAdmin_Returns200() throws Exception {
        when(alertService.getPendingNotifications()).thenReturn(List.of(WeatherAlert.builder().build(), WeatherAlert.builder().build()));

        mockMvc.perform(get("/weather/alerts/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    @Order(6)
    @DisplayName("6. GET /weather/alerts/pending - 403 for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testGetPendingNotifications_AsProducer_Returns403() throws Exception {
        mockMvc.perform(get("/weather/alerts/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    @DisplayName("7. GET /weather/alerts/pending - 401 when unauthenticated")
    void testGetPendingNotifications_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/weather/alerts/pending"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Order(8)
    @DisplayName("8. GET /weather/alerts/statistics/{id} - 200 OK for SUPPLIER_ADMIN")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testGetAlertStatistics_Returns200() throws Exception {
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(buildLocation()));
        when(alertService.getAlertStatistics(any())).thenReturn(Map.of("total", 5, "critical", 1));

        mockMvc.perform(get("/weather/alerts/statistics/{id}", LOCATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.critical").value(1));
    }

    @Test
    @Order(9)
    @DisplayName("9. GET /weather/alerts/statistics/{id} - 400 when location not found")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testGetAlertStatistics_LocationNotFound_Returns400() throws Exception {
        when(locationRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/weather/alerts/statistics/{id}", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("10. POST /weather/alerts/{id}/resolve - 200 OK for SYSTEM_ADMIN")
    @WithMockUser(username = "admin@agrowerk.tech", authorities = "SYSTEM_ADMIN")
    void testResolveAlert_AsSystemAdmin_Returns200() throws Exception {
        doNothing().when(alertService).resolveAlert(any(), anyString());

        mockMvc.perform(post("/weather/alerts/{id}/resolve", ALERT_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alert resolved successfully"))
                .andExpect(jsonPath("$.alertId").value(ALERT_ID.toString()))
                .andExpect(jsonPath("$.resolvedBy").value("admin@agrowerk.tech"));

        verify(alertService).resolveAlert(ALERT_ID, "admin@agrowerk.tech");
    }

    @Test
    @Order(11)
    @DisplayName("11. POST /weather/alerts/{id}/resolve - 200 OK for PRODUCER")
    @WithMockUser(username = "producer@agrowerk.tech", authorities = "PRODUCER")
    void testResolveAlert_AsProducer_Returns200() throws Exception {
        doNothing().when(alertService).resolveAlert(any(), anyString());

        mockMvc.perform(post("/weather/alerts/{id}/resolve", ALERT_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedBy").value("producer@agrowerk.tech"));
    }

    @Test
    @Order(12)
    @DisplayName("12. POST /weather/alerts/{id}/resolve - 403 for SUPPLIER_ADMIN")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testResolveAlert_AsSupplierAdmin_Returns403() throws Exception {
        mockMvc.perform(post("/weather/alerts/{id}/resolve", ALERT_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    @DisplayName("13. POST /weather/alerts/{id}/resolve - 401 when unauthenticated")
    void testResolveAlert_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/weather/alerts/{id}/resolve", ALERT_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}