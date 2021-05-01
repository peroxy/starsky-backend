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
}
