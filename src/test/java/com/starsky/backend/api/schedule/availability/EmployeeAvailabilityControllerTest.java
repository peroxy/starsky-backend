package com.starsky.backend.api.schedule.availability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.schedule.shift.ScheduleShiftResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeAvailabilityControllerTest extends TestJwtProvider {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private ScheduleShiftResponse shiftWithAvailabilities;
    private ScheduleShiftResponse shiftWithoutAvailabilities;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("scheduling@a.com", "password"), new LoginRequest("scheduling@1.com", "password"));

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        shiftWithAvailabilities = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse.class);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/6")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        shiftWithoutAvailabilities = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse.class);
    }

    @Test
    public void managerShouldGetEmployeeAvailability() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse.class);
        Assertions.assertNotNull(availabilityResponse);
    }

    @Test
    public void shouldGetNotFoundWhenGettingNonExistentAvailability() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/availabilities/574839")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.get("/user/availabilities/574839")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void employeeShouldGetAvailability() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse.class);
        Assertions.assertNotNull(availabilityResponse);
    }

    @Test
    public void managerShouldGetEmployeeAvailabilities() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/%d/availabilities".formatted(shiftWithAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponses = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse[].class);
        Assertions.assertEquals(20, availabilityResponses.length); //19 scheduling employees should each have 1 availability
    }

    @Test
    public void shouldGetNotFoundWhenGettingNonExistentShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/574839/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void managerShouldGetNoAvailabilities() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponses = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse[].class);
        Assertions.assertEquals(0, availabilityResponses.length);
    }

    @Test
    public void employeeShouldGetNoAvailabilities() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponses = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse[].class);
        Assertions.assertEquals(0, availabilityResponses.length);
    }

    @Test
    public void employeeShouldGetAvailabilities() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/%d/availabilities".formatted(shiftWithAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponses = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse[].class);
        Assertions.assertEquals(20, availabilityResponses.length);
    }

    private CreateEmployeeAvailabilityRequest getEmployeeAvailabilityRequest() {
        var start = Instant.now();
        var end = start.plus(Duration.ofHours(10));
        var maxHoursPerShift = 8;
        return new CreateEmployeeAvailabilityRequest(start, end, maxHoursPerShift, 8);
    }

    @Test
    @Transactional
    public void shouldCreateEmployeeAvailability() throws Exception {
        var request = getEmployeeAvailabilityRequest();
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse.class);
        Assertions.assertEquals(request.getAvailabilityStart(), availabilityResponse.getAvailabilityStart());
        Assertions.assertEquals(request.getAvailabilityEnd(), availabilityResponse.getAvailabilityEnd());
        Assertions.assertEquals(request.getMaxHoursPerShift(), availabilityResponse.getMaxHoursPerShift());
        Assertions.assertEquals(request.getEmployeeId(), availabilityResponse.getEmployeeId());
    }

    @Test
    @Transactional
    public void shouldGetBadRequestWhenCreatingInvalidEmployeeAvailability() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("i am not a json"))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        var start = Instant.now();
        var end = start.plus(Duration.ofHours(10));
        var request = new CreateEmployeeAvailabilityRequest(start, end, 0, 8);
        mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void shouldGetNotFoundWhenCreatingWithNonExistentShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/3487438/availabilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getEmployeeAvailabilityRequest()))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldGetUnprocessableEntityWhenCreatingAvailabilityWithInvalidDateRange() throws Exception {
        var end = Instant.now();
        var start = end.plus(Duration.ofHours(10));
        var request = new CreateEmployeeAvailabilityRequest(start, end, 1, 8);
        mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void employeeShouldGetForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getEmployeeAvailabilityRequest()))
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getUpdateEmployeeAvailabilityRequest()))
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getUpdateEmployeeAvailabilityRequest()))
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldUpdateEmployeeAvailability() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse.class);

        var updatedShift = getUpdateEmployeeAvailabilityRequest();
        result = mockMvc.perform(MockMvcRequestBuilders.patch("/user/availabilities/%d".formatted(availabilityResponse.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedShift))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
        var updatedAvailability = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse.class);

        Assertions.assertEquals(updatedShift.getAvailabilityStart().get(), updatedAvailability.getAvailabilityStart());
        Assertions.assertEquals(updatedShift.getAvailabilityEnd().get(), updatedAvailability.getAvailabilityEnd());
        Assertions.assertEquals(updatedShift.getMaxHoursPerShift().get(), updatedAvailability.getMaxHoursPerShift());
        // employee ID cannot be updated, no need to check
    }

    @Test
    public void shouldGetBadRequestWhenUpdating() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("bad json"))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateEmployeeAvailabilityRequest(Instant.now(), Instant.now(), -1)))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetNotFoundWhenUpdating() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/availabilities/156434")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getUpdateEmployeeAvailabilityRequest()))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetUnprocessableEntityWhenUpdating() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getInvalidUpdateAvailabilityRequest()))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void shouldGetNotFoundWhenDeleting() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/availabilities/156434")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldDeleteScheduleShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/availabilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    private UpdateEmployeeAvailabilityRequest getUpdateEmployeeAvailabilityRequest() {
        var start = Instant.now().plus(Duration.ofHours(20));
        var end = start.plus(Duration.ofHours(5));
        var maxHours = 10;
        return new UpdateEmployeeAvailabilityRequest(start, end, maxHours);
    }

    private UpdateEmployeeAvailabilityRequest getInvalidUpdateAvailabilityRequest() {
        var end = Instant.now().plus(Duration.ofHours(20));
        var start = end.plus(Duration.ofHours(5));
        var maxHours = 6;
        return new UpdateEmployeeAvailabilityRequest(start, end, maxHours);
    }

    @Test
    @DirtiesContext
    public void shouldGetUnprocessableEntityWhenCreatingOverlappingAvailability() throws Exception {
        var request = getEmployeeAvailabilityRequest();
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse.class);
        Assertions.assertEquals(request.getAvailabilityStart(), availabilityResponse.getAvailabilityStart());
        Assertions.assertEquals(request.getAvailabilityEnd(), availabilityResponse.getAvailabilityEnd());
        Assertions.assertEquals(request.getMaxHoursPerShift(), availabilityResponse.getMaxHoursPerShift());
        Assertions.assertEquals(request.getEmployeeId(), availabilityResponse.getEmployeeId());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        var start = Instant.now().plus(Duration.ofHours(2));
        var end = start.plus(Duration.ofHours(4));

        mockMvc.perform(MockMvcRequestBuilders.post("/user/shifts/%d/availabilities".formatted(shiftWithoutAvailabilities.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateEmployeeAvailabilityRequest(start, end, 8, 8)))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }


}

