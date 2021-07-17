package com.starsky.backend.api.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.schedule.assignment.EmployeeAssignmentResponse;
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

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
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

    private TeamResponse team;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("scheduling@a.com", "password"), new LoginRequest("scheduling@1.com", "password"));

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var teamResponses = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, teamResponses.length);
        this.team = teamResponses[0];
    }

    @Test
    public void managerShouldGetScheduleById() throws Exception {
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
    public void employeeShouldGetScheduleById() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
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
    public void employeeShouldGetForbiddenManagerScheduleRoutes() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/1/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("something")
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("something")
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void managerShouldGetAllSchedules() throws Exception {
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
    public void shouldGetSolvedSchedule() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAssignmentResponse[].class);
    }

    @Test
    public void employeeShouldGetAllSchedules() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse[].class);
        Assertions.assertEquals(2, scheduleResponse.length);
        Assertions.assertTrue(Arrays.stream(scheduleResponse).anyMatch(resp -> resp.getScheduleName().equals("Test schedule 1") || resp.getScheduleName().equals("Test schedule 2")));
    }

    @Test
    public void shouldGetAllSchedulesFilteredByTeam() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules?team_id=%d".formatted(team.getId()))
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

    @Test
    @Transactional
    public void shouldCreateSchedule() throws Exception {
        createSchedule();
    }

    @Test
    public void shouldCreateInvalidSchedules() throws Exception {
        var validRequest = new CreateScheduleRequest("asd", Instant.now(), Instant.now().plus(Duration.ofDays(10)), 80, 10, 10);
        var brokenRequest = new CreateScheduleRequest(null, Instant.now(), Instant.now().plus(Duration.ofDays(10)), 80, 10, 10);
        var invalidRequest = new CreateScheduleRequest("asd", Instant.now().plus(Duration.ofDays(10)), Instant.now(), 80, 10, 10);
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/%d/schedules".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(brokenRequest))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/%d/schedules".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/123346/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateAndDeleteSchedule() throws Exception {
        ScheduleResponse scheduleResponse = createSchedule();

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/schedules/%d".formatted(scheduleResponse.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/%d".formatted(scheduleResponse.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetNotFoundWhenDeletingNonExistentSchedule() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/schedules/5749839")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldUpdateSchedule() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse.class);

        result = mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UpdateScheduleRequest("updated schedule", null, null, null, null, null, null)))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var updateResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse.class);
        Assertions.assertNotEquals(scheduleResponse.getScheduleName(), updateResponse.getScheduleName());
        Assertions.assertEquals("updated schedule", updateResponse.getScheduleName());
        Assertions.assertEquals(scheduleResponse.getScheduleStart(), updateResponse.getScheduleStart());
        Assertions.assertEquals(scheduleResponse.getScheduleEnd(), updateResponse.getScheduleEnd());
        Assertions.assertEquals(scheduleResponse.getMaxHoursPerEmployee(), updateResponse.getMaxHoursPerEmployee());
        Assertions.assertEquals(scheduleResponse.getMaxShiftsPerEmployee(), updateResponse.getMaxShiftsPerEmployee());
        Assertions.assertEquals(scheduleResponse.getMaxHoursPerShift(), updateResponse.getMaxHoursPerShift());
    }

    @Test
    public void shouldGetErrorsWhenUpdatingInvalidSchedules() throws Exception {
        var now = Instant.now();
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1584848")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UpdateScheduleRequest(null, now, now, null, null, null, null)))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("i am an invalid body")
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UpdateScheduleRequest(null, now, now, null, null, null, null)))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    private ScheduleResponse createSchedule() throws Exception {
        var request = new CreateScheduleRequest("testing", Instant.now(), Instant.now().plus(Duration.ofDays(10)), 80, 10, 10);
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/%d/schedules".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var scheduleResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleResponse.class);
        Assertions.assertEquals(request.getScheduleName(), scheduleResponse.getScheduleName());
        Assertions.assertEquals(request.getScheduleStart(), scheduleResponse.getScheduleStart());
        Assertions.assertEquals(request.getScheduleEnd(), scheduleResponse.getScheduleEnd());
        Assertions.assertEquals(request.getMaxHoursPerEmployee(), scheduleResponse.getMaxHoursPerEmployee());
        Assertions.assertEquals(request.getMaxHoursPerShift(), scheduleResponse.getMaxHoursPerShift());
        Assertions.assertEquals(request.getMaxShiftsPerEmployee(), scheduleResponse.getMaxShiftsPerEmployee());
        return scheduleResponse;
    }

}
