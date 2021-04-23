package com.starsky.backend.api.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.team.TeamResponse;
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

import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScheduleControllerTest extends TestJwtProvider {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("scheduling@a.com", "password"), new LoginRequest("scheduling@1.com", "password"));
    }

    @Test
    public void shouldGetScheduleById() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse.class);
        Assertions.assertEquals("Test schedule 1", scheduleResponse.getScheduleName());
    }

    @Test
    public void shouldGetNotFoundForNonExistentSchedule() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/15467898")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void employeeShouldGetForbiddenOnAllScheduleRoutes() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
        //TODO: add other routes
    }

    @Test
    public void shouldGetAllSchedules() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse[].class);
        Assertions.assertEquals(2, scheduleResponse.length);
        Assertions.assertTrue(Arrays.stream(scheduleResponse).anyMatch(resp -> resp.getScheduleName().equals("Test schedule 1") || resp.getScheduleName().equals("Test schedule 2")));
    }

    @Test
    public void shouldGetAllSchedulesFilteredByTeam() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var teamResponses = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, teamResponses.length);
        var team = teamResponses[0];

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules?team_id=%d".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse[].class);
        Assertions.assertEquals(2, scheduleResponse.length);
        Assertions.assertTrue(Arrays.stream(scheduleResponse).anyMatch(resp -> resp.getScheduleName().equals("Test schedule 1") || resp.getScheduleName().equals("Test schedule 2")));

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules?team_id=%d".formatted(team.getId() + 1)) //this team wont exist
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse[].class);
        Assertions.assertEquals(0, scheduleResponse.length);
    }

    //TODO: finish POST, DELETE, PATCh tests, make sure to tag them as @Transactional

}
