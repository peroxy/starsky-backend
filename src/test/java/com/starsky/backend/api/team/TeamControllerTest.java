package com.starsky.backend.api.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
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

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("Should get manager's teams")
    public void testGetManagerTeams() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams")
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
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams")
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
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/user/teams")
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
    @DisplayName("Should get bad request when creating a new team")
    public void testCreateTeamBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\":1}")
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get conflict when creating a new team with existing name")
    public void testCreateExistingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("Test Team")))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Employee should get forbidden when creating a new team")
    public void testEmployeeCreateNewTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("new test team")))
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Not authenticated user should get forbidden when creating a new team")
    public void testNoAuthenticationCreateNewTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("new test team"))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
