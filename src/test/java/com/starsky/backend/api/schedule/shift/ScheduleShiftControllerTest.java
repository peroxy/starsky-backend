package com.starsky.backend.api.schedule.shift;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.schedule.ScheduleResponse;
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
public class ScheduleShiftControllerTest extends TestJwtProvider {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private ScheduleResponse scheduleWithShifts;
    private ScheduleResponse scheduleWithoutShifts;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("scheduling@a.com", "password"), new LoginRequest("scheduling@1.com", "password"));

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        scheduleWithShifts = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse.class);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        scheduleWithoutShifts = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse.class);
    }

    @Test
    public void managerShouldGetScheduleShifts() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/%d/shifts".formatted(scheduleWithShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse[].class);
        Assertions.assertEquals(9, scheduleResponse.length);
    }

    @Test
    public void shouldGetNotFoundWhenGettingNonExistentSchedule() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/574839/shifts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void managerShouldGetNoScheduleShifts() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/%d/shifts".formatted(scheduleWithoutShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse[].class);
        Assertions.assertEquals(0, scheduleResponse.length);
    }

    @Test
    public void employeeShouldGetNoScheduleShifts() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/%d/shifts".formatted(scheduleWithoutShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse[].class);
        Assertions.assertEquals(0, scheduleResponse.length);
    }

    @Test
    public void employeeShouldGetScheduleShifts() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/%d/shifts".formatted(scheduleWithShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var shiftResponses = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse[].class);
        Assertions.assertEquals(9, shiftResponses.length);
    }

    private CreateScheduleShiftRequest getValidRequest() {
        var start = Instant.now();
        var end = start.plus(Duration.ofHours(10));
        var numberOfRequiredEmployees = 10;
        return new CreateScheduleShiftRequest(start, end, numberOfRequiredEmployees);
    }

    @Test
    @Transactional
    public void shouldCreateScheduleShift() throws Exception {
        var request = getValidRequest();
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/%d/shifts".formatted(scheduleWithoutShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var shiftResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse.class);
        Assertions.assertEquals(request.getShiftStart(), shiftResponse.getShiftStart());
        Assertions.assertEquals(request.getShiftEnd(), shiftResponse.getShiftEnd());
        Assertions.assertEquals(request.getNumberOfRequiredEmployees(), shiftResponse.getNumberOfRequiredEmployees());
    }

    @Test
    @Transactional
    public void shouldGetBadRequestWhenCreatingInvalidScheduleShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/%d/shifts".formatted(scheduleWithoutShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("i am not a json"))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        var start = Instant.now();
        var end = start.plus(Duration.ofHours(10));
        var request = new CreateScheduleShiftRequest(start, end, 0);
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/%d/shifts".formatted(scheduleWithoutShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void shouldGetNotFoundWhenCreatingNonExistentSchedule() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/575748/shifts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getValidRequest()))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldGetUnprocessableEntityWhenCreatingScheduleShiftWithInvalidDateRange() throws Exception {
        var end = Instant.now();
        var start = end.plus(Duration.ofHours(10));
        var request = new CreateScheduleShiftRequest(start, end, 1);
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/%d/shifts".formatted(scheduleWithoutShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void employeeShouldGetForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/%d/shifts".formatted(scheduleWithoutShifts.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getValidRequest()))
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


}
