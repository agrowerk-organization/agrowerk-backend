package tech.agrowerk.application.controller.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.agrowerk.application.controller.base.BaseControllerTest;
import tech.agrowerk.application.dto.weather.*;
import tech.agrowerk.infrastructure.client.OpenMeteoClient;
import tech.agrowerk.business.service.weather.WeatherCacheService;
import tech.agrowerk.business.service.weather.WeatherDashboardService;
import tech.agrowerk.business.service.weather.WeatherService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WeatherCacheService cacheService;

    @MockitoBean
    private WeatherDashboardService dashboardService;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private OpenMeteoClient openMeteoClient;

    private static final UUID LOCATION_ID = UUID.randomUUID();

    @Override
    public void setUp() {
        super.setUpSecurity();
    }

    @Test
    @Order(1)
    @DisplayName("1. GET /weather/current/{id} - 200 OK for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testGetCurrentWeather_AsProducer_Returns200() throws Exception {
        when(cacheService.getCurrentWeather(LOCATION_ID)).thenReturn(Current.builder().build());

        mockMvc.perform(get("/weather/current/{id}", LOCATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /weather/current/{id} - 403 for unauthorized role")
    @WithMockUser(authorities = "UNKNOWN_ROLE")
    void testGetCurrentWeather_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(get("/weather/current/{id}", LOCATION_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /weather/current/{id} - 401 when unauthenticated")
    void testGetCurrentWeather_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/weather/current/{id}", LOCATION_ID))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Order(4)
    @DisplayName("4. GET /weather/forecast/{id} - 200 OK with default 7 days for SUPPLIER_ADMIN")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testGetForecast_DefaultDays_Returns200() throws Exception {
        when(cacheService.getForecast(any(), anyInt())).thenReturn(List.of(Forecast.builder().build()));

        mockMvc.perform(get("/weather/forecast/{id}", LOCATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /weather/forecast/{id}?days=3 - 200 OK with custom days param")
    @WithMockUser(authorities = "PRODUCER")
    void testGetForecast_CustomDays_Returns200() throws Exception {
        when(cacheService.getForecast(LOCATION_ID, 3)).thenReturn(List.of(Forecast.builder().build(), Forecast.builder().build(), Forecast.builder().build()));

        mockMvc.perform(get("/weather/forecast/{id}", LOCATION_ID)
                        .param("days", "3"))
                .andExpect(status().isOk());

        verify(cacheService).getForecast(LOCATION_ID, 3);
    }

    @Test
    @Order(6)
    @DisplayName("6. GET /weather/forecast/{id} - 403 for unauthorized role")
    @WithMockUser(authorities = "UNKNOWN_ROLE")
    void testGetForecast_UnauthorizedRole_Returns403() throws Exception {
        mockMvc.perform(get("/weather/forecast/{id}", LOCATION_ID))
                .andExpect(status().isForbidden());
    }


    @Test
    @Order(7)
    @DisplayName("7. GET /weather/alerts/{id} - 200 OK for SYSTEM_ADMIN")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testGetActiveAlerts_AsSystemAdmin_Returns200() throws Exception {
        when(cacheService.getActiveAlerts(LOCATION_ID)).thenReturn(List.of(Alert.builder().build()));

        mockMvc.perform(get("/weather/alerts/{id}", LOCATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("8. GET /weather/alerts/{id} - 401 when unauthenticated")
    void testGetActiveAlerts_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/weather/alerts/{id}", LOCATION_ID))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Order(9)
    @DisplayName("9. GET /weather/dashboard/{id} - 200 OK for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testGetDashboard_AsProducer_Returns200() throws Exception {
        when(dashboardService.getDashboard(LOCATION_ID)).thenReturn(Dashboard.builder().build());

        mockMvc.perform(get("/weather/dashboard/{id}", LOCATION_ID))
                .andExpect(status().isOk());
    }


    @Test
    @Order(10)
    @DisplayName("10. GET /weather/statistics/{id} - 200 OK for SUPPLIER_ADMIN")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testGetStatistics_AsSupplierAdmin_Returns200() throws Exception {
        when(cacheService.calculateStatistics(LOCATION_ID)).thenReturn(Statistics.builder().build());

        mockMvc.perform(get("/weather/statistics/{id}", LOCATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @WithMockUser
    @DisplayName("11. GET /weather/health - 200 OK when circuit breaker is CLOSED")
    void testHealthCheck_CircuitClosed_Returns200() throws Exception {
        when(openMeteoClient.getCircuitBreakerState()).thenReturn("CLOSED");
        when(openMeteoClient.getMetrics()).thenReturn(new CircuitBreakerMetrics(
                "CLOSED",
                0.0f,
                0.0f,
                0,
                0,
                0
        ));

        mockMvc.perform(get("/weather/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.circuitBreaker.state").value("CLOSED"));
    }

    @Test
    @Order(12)
    @WithMockUser
    @DisplayName("12. GET /weather/health - 503 when circuit breaker is OPEN")
    void testHealthCheck_CircuitOpen_Returns503() throws Exception {
        when(openMeteoClient.getCircuitBreakerState()).thenReturn("OPEN");
        when(openMeteoClient.getMetrics()).thenReturn(new CircuitBreakerMetrics(
                "CLOSED",
                0.0f,
                0.0f,
                0,
                0,
                0
        ));
        mockMvc.perform(get("/weather/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.circuitBreaker.state").value("OPEN"));
    }



    @Test
    @Order(13)
    @DisplayName("13. POST /weather/refresh/{id} - 200 OK for SYSTEM_ADMIN")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testForceRefresh_AsSystemAdmin_Returns200() throws Exception {
        when(weatherService.getCurrentWeatherInternal(any())).thenReturn(Current.builder().build());

        mockMvc.perform(post("/weather/refresh/{id}", LOCATION_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Weather data refreshed"));
    }

    @Test
    @Order(14)
    @DisplayName("14. POST /weather/refresh/{id} - 403 for PRODUCER (insufficient authority)")
    @WithMockUser(authorities = "PRODUCER")
    void testForceRefresh_AsProducer_Returns403() throws Exception {
        mockMvc.perform(post("/weather/refresh/{id}", LOCATION_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}