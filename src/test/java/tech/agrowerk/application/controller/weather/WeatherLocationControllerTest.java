package tech.agrowerk.application.controller.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.agrowerk.application.controller.base.BaseControllerTest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationCreateRequest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationDto;
import tech.agrowerk.application.dto.weather.location.WeatherLocationUpdateRequest;
import tech.agrowerk.business.service.weather.WeatherLocationService;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherLocationController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherLocationControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WeatherLocationService locationService;

    private static final UUID WEATHER_LOCATION_ID = UUID.randomUUID();
    private static final UUID LOCATION_ID = UUID.randomUUID();

    @Override
    protected void setUp() {
        super.setUpSecurity();
    }

    private WeatherLocationDto buildLocationDto() {
        return new WeatherLocationDto(
                WEATHER_LOCATION_ID,
                "Horto - Juazeiro do Norte",
                new BigDecimal("-7.1895"),
                new BigDecimal("-39.3328"),
                "CE",
                "BR",
                "America/Fortaleza",
                LOCATION_ID,
                "Horto",
                true,
                Instant.now(),
                Instant.now()
        );
    }


    @Test
    @Order(1)
    @DisplayName("1. GET /weather/locations - 200 OK, returns active locations for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testGetAllLocations_ActiveOnly_Returns200() throws Exception {
        when(locationService.findActiveLocations()).thenReturn(List.of(buildLocationDto()));

        mockMvc.perform(get("/weather/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(LOCATION_ID.toString()))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("2. GET /weather/locations?activeOnly=false - 200 OK, returns all locations")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testGetAllLocations_All_Returns200() throws Exception {
        when(locationService.findAllLocations()).thenReturn(List.of(buildLocationDto()));

        mockMvc.perform(get("/weather/locations")
                        .param("activeOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(locationService).findAllLocations();
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /weather/locations - 401 when unauthenticated")
    void testGetAllLocations_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/weather/locations"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Order(4)
    @DisplayName("4. GET /weather/locations/{id} - 200 OK for SUPPLIER_ADMIN")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testGetLocationById_Found_Returns200() throws Exception {
        when(locationService.findById(LOCATION_ID)).thenReturn(buildLocationDto());

        mockMvc.perform(get("/weather/locations/{id}", LOCATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LOCATION_ID.toString()))
                .andExpect(jsonPath("$.name").value("Horto - Juazeiro do Norte"));
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /weather/locations/{id} - 404 when location does not exist")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testGetLocationById_NotFound_Returns404() throws Exception {
        when(locationService.findById(any()))
                .thenThrow(new EntityNotFoundException("Location not found"));

        mockMvc.perform(get("/weather/locations/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }


    @Test
    @Order(6)
    @DisplayName("6. GET /weather/locations/property/{propertyId} - 200 OK for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testGetLocationByProperty_Returns200() throws Exception {
        UUID propertyId = UUID.randomUUID();
        when(locationService.findByPropertyId(propertyId)).thenReturn(buildLocationDto());

        mockMvc.perform(get("/weather/locations/property/{propertyId}", propertyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LOCATION_ID.toString()));
    }


    @Test
    @Order(7)
    @DisplayName("7. POST /weather/locations - 201 Created for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testCreateLocation_AsProducer_Returns201() throws Exception {
        WeatherLocationCreateRequest request = new WeatherLocationCreateRequest(
                "Santo Antônio",
                new BigDecimal("-7.1895"),
                new BigDecimal("-39.3328"),
                "CE",
                "BR",
                "America/Fortaleza",
                LOCATION_ID,
                true
        );

        when(locationService.createLocation(any())).thenReturn(buildLocationDto());

        mockMvc.perform(post("/weather/locations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(LOCATION_ID.toString()));
    }

    @Test
    @Order(8)
    @DisplayName("8. POST /weather/locations - 403 for SUPPLIER_ADMIN (insufficient authority)")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testCreateLocation_AsSupplierAdmin_Returns403() throws Exception {
        WeatherLocationCreateRequest request = new WeatherLocationCreateRequest(
                "Santo Antônio",
                new BigDecimal("-7.1895"),
                new BigDecimal("-39.3328"),
                "CE",
                "BR",
                "America/Fortaleza",
                LOCATION_ID,
                true
        );

        mockMvc.perform(post("/weather/locations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }


    @Test
    @Order(9)
    @DisplayName("9. PUT /weather/locations/{id} - 200 OK for SYSTEM_ADMIN")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testUpdateLocation_AsSystemAdmin_Returns200() throws Exception {
        WeatherLocationUpdateRequest request = new WeatherLocationUpdateRequest("Nova propriedade", "America/Fortaleza", null, true);

        when(locationService.updateLocation(eq(LOCATION_ID), any())).thenReturn(buildLocationDto());

        mockMvc.perform(put("/weather/locations/{id}", LOCATION_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    @DisplayName("10. PUT /weather/locations/{id} - 403 for SUPPLIER_ADMIN")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testUpdateLocation_AsSupplierAdmin_Returns403() throws Exception {
        mockMvc.perform(put("/weather/locations/{id}", LOCATION_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }


    @Test
    @Order(11)
    @DisplayName("11. PATCH /weather/locations/{id}/activate - 200 OK for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testActivateLocation_AsProducer_Returns200() throws Exception {
        doNothing().when(locationService).setActive(LOCATION_ID, true);

        mockMvc.perform(patch("/weather/locations/{id}/activate", LOCATION_ID)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(locationService).setActive(LOCATION_ID, true);
    }

    @Test
    @Order(12)
    @DisplayName("12. PATCH /weather/locations/{id}/activate - 401 when unauthenticated")
    void testActivateLocation_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(patch("/weather/locations/{id}/activate", LOCATION_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Order(13)
    @DisplayName("13. DELETE /weather/locations/{id} - 204 No Content for SYSTEM_ADMIN")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testDeleteLocation_AsSystemAdmin_Returns204() throws Exception {
        doNothing().when(locationService).deleteLocation(LOCATION_ID);

        mockMvc.perform(delete("/weather/locations/{id}", LOCATION_ID)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(14)
    @DisplayName("14. DELETE /weather/locations/{id} - 403 for PRODUCER")
    @WithMockUser(authorities = "PRODUCER")
    void testDeleteLocation_AsProducer_Returns403() throws Exception {
        mockMvc.perform(delete("/weather/locations/{id}", LOCATION_ID)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}