package com.starsky.backend.api.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.user.UserResponse;
import org.junit.jupiter.api.*;
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
import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TeamControllerTest extends TestJwtProvider {

    private final static String TEAMS_ROUTE = "/user/teams";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("a@a.com", "password"), new LoginRequest("t@t.com", "password"));
    }

    @Test
    @DisplayName("Should get manager's teams")
    public void testGetManagerTeams() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, response.length);
        Assertions.assertEquals("Test Manager", response[0].getOwnerName());
        Assertions.assertEquals("Test Team", response[0].getName());
    }

    @Test
    @DisplayName("Should get employee's teams")
    public void testGetEmployeeTeams() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(2, response.length);
        Assertions.assertTrue(Arrays.stream(response).anyMatch(teamResponse -> teamResponse.getOwnerName().equals("Test Manager")));
        Assertions.assertTrue(Arrays.stream(response).anyMatch(teamResponse -> teamResponse.getOwnerName().equals("Harold C. Dobey")));
        Assertions.assertTrue(Arrays.stream(response).anyMatch(teamResponse -> teamResponse.getName().equals("Test Team")));
        Assertions.assertTrue(Arrays.stream(response).anyMatch(teamResponse -> teamResponse.getName().equals("Harold's Detectives")));
    }

    @Test
    @DisplayName("Should create a new team")
    @Transactional
    public void testCreateNewTeam() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.post(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("new test team")))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse.class);
        Assertions.assertEquals("Test Manager", response.getOwnerName());
        Assertions.assertEquals("new test team", response.getName());
    }

    @Test
    @DisplayName("Should add a new team member")
    @Transactional
    public void testCreateTeamMember() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var teamResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, teamResponse.length);
        var team = teamResponse[0];

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams/%d/members".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var membersResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(1, membersResponse.length);
        var teamMember = membersResponse[0];

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employeesResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(2, employeesResponse.length);

        var nonTeamMember = Arrays.stream(employeesResponse).filter(userResponse -> userResponse.getId() != teamMember.getId()).findFirst();
        Assertions.assertTrue(nonTeamMember.isPresent());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/%d/members/%d".formatted(team.getId(), nonTeamMember.get().getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams/%d/members".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        membersResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(2, membersResponse.length);
    }

    @Test
    @Transactional
    public void testCreateTeamMembers() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var teamResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, teamResponse.length);
        var team = teamResponse[0];

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employeesResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(2, employeesResponse.length);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/teams/%d/members".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateTeamMemberRequest[]{
                                new CreateTeamMemberRequest(employeesResponse[0].getId()),
                                new CreateTeamMemberRequest(employeesResponse[1].getId())}))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get conflict when adding an existing member to team")
    public void testAddExistingTeamMember() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var teamResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, teamResponse.length);
        var team = teamResponse[0];

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams/%d/members".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var membersResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(1, membersResponse.length);
        var teamMember = membersResponse[0];

        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/%d/members/%d".formatted(team.getId(), teamMember.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should get not found for non-existent team")
    public void testCreateTeamMemberTeamNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/12345678/members/456789")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get not found for non-existent employee")
    public void testCreateTeamMemberEmployeeNotFound() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var teamResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, teamResponse.length);
        var team = teamResponse[0];

        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams/%d/members/1234567".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get not found when requesting non existent team")
    public void testGetTeamMembersNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/teams/123456/members")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get team members")
    public void testGetTeamMembers() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, response.length);
        var team = response[0];

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams/%d/members".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var teamMembersResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(1, response.length);
        Assertions.assertEquals("Test Employee", teamMembersResponse[0].getName());
        Assertions.assertEquals("EMPLOYEE", teamMembersResponse[0].getRole());
        Assertions.assertEquals("t@t.com", teamMembersResponse[0].getEmail());
    }

    @Test
    @DisplayName("Should get bad request when creating a new team")
    public void testCreateTeamBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\":1}")
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get conflict when creating a new team with existing name")
    public void testCreateExistingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("Test Team")))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Employee should get forbidden when creating a new team")
    public void testEmployeeCreateNewTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("new test team")))
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Not authenticated user should get forbidden when creating a new team")
    public void testNoAuthenticationCreateNewTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("new test team"))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldDeleteTeam() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, response.length);
        var team = response[0];
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/teams/%d".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldGetNotFoundWhenDeletingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/teams/12445564")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void employeeShouldGetForbiddenWhenDeletingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/teams/12445564")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldDeleteTeamMember() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, response.length);
        var team = response[0];

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams/%d/members".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var membersResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(1, membersResponse.length);
        var teamMember = membersResponse[0];

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/teams/%d/members/%d".formatted(team.getId(), teamMember.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldGetNotFoundWhenDeletingTeamMember() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, response.length);
        var team = response[0];

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/teams/12334/members/48582")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/teams/%d/members/34834578".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void employeeShouldGetForbiddenWhenDeletingTeamMember() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/teams/1/members/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void employeeShouldGetForbiddenWhenUpdatingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/teams/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldUpdateTeam() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get(TEAMS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse[].class);
        Assertions.assertEquals(1, response.length);
        var team = response[0];

        var request = new UpdateTeamRequest("new team name");
        result = mockMvc.perform(MockMvcRequestBuilders.patch("/user/teams/%d".formatted(team.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var updatedTeam = objectMapper.readValue(result.getResponse().getContentAsString(), TeamResponse.class);
        Assertions.assertEquals(request.getName().get(), updatedTeam.getName());
    }

    @Test
    public void shouldGetBadRequestWhenUpdatingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/teams/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("i am not a valid json")
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.patch("/user/teams/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateTeamRequest("     ")))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetNotFoundWhenUpdatingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/teams/154987534789345789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateTeamRequest("new name")))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());

    }
}
