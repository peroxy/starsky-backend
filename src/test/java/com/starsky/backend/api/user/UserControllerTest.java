package com.starsky.backend.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.authentication.TokenResponse;
import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.api.invite.InviteResponse;
import com.starsky.backend.api.team.CreateTeamRequest;
import com.starsky.backend.api.team.TeamResponse;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.User;
import com.starsky.backend.repository.InviteRepository;
import com.starsky.backend.service.invite.InviteService;
import org.junit.jupiter.api.*;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

    @Autowired
    ObjectProvider<InviteRepository> inviteRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private InviteService inviteService;

    private String managerJwtHeader;
    private String employeeJwtHeader;

    @BeforeAll
    void setup() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("a@a.com", "password"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var tokenResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);
        Assertions.assertEquals("Bearer", tokenResponse.getTokenType());
        Assertions.assertNotNull(tokenResponse.getAccessToken());
        Assertions.assertNotNull(tokenResponse.getExpiresOn());
        managerJwtHeader = "%s %s".formatted(tokenResponse.getTokenType(), tokenResponse.getAccessToken());

        result = mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("t@t.com", "password"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        tokenResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);
        Assertions.assertEquals("Bearer", tokenResponse.getTokenType());
        Assertions.assertNotNull(tokenResponse.getAccessToken());
        Assertions.assertNotNull(tokenResponse.getExpiresOn());
        employeeJwtHeader = "%s %s".formatted(tokenResponse.getTokenType(), tokenResponse.getAccessToken());
    }

    @BeforeEach
    void setupEach() {
        doReturn(null).when(inviteService).sendInviteToMailApi(any(User.class), any(CreateInviteRequest.class), any(Invite.class));
    }

    @Test
    @DisplayName("Should create new manager")
    @Transactional
    public void testCreateNewManager() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "david@starsky.com",
                                        "veryLooooooooooooooooooongPassword",
                                        "Police Detective",
                                        null))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
        Assertions.assertEquals("david@starsky.com", response.getEmail());
        Assertions.assertEquals("Police Detective", response.getJobTitle());
        Assertions.assertEquals("David Starsky", response.getName());
        Assertions.assertEquals("EMAIL", response.getNotificationType());
        Assertions.assertEquals("MANAGER", response.getRole());
        Assertions.assertNull(response.getPhoneNumber()); //atm phone numbers are only supported in database, but not when actually registering
    }

    @Test
    @DisplayName("Duplicated user email should get conflict response")
    public void testCreateNewManagerWithExistingMail() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "test@mailer.com",
                                        "veryLooooooooooooooooooongPassword",
                                        "Police Detective",
                                        null))))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "test@mailer.com",
                                        "veryLooooooooooooooooooongPassword",
                                        "Police Detective",
                                        null))))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should get the authenticated user")
    public void testGetAuthenticatedUser() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.get("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", managerJwtHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var userResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
        Assertions.assertEquals("a@a.com", userResponse.getEmail());
        Assertions.assertEquals("Test Manager", userResponse.getName());
        Assertions.assertEquals("Test Manager", userResponse.getJobTitle());
        Assertions.assertEquals("MANAGER", userResponse.getRole());
        Assertions.assertEquals("EMAIL", userResponse.getNotificationType());
        Assertions.assertNull(userResponse.getPhoneNumber());
    }

    @Test
    @DisplayName("Should get employees")
    public void testGetAuthenticatedUserEmployees() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", managerJwtHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var employees = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse[].class);
        Assertions.assertEquals(1, employees.length);
        Assertions.assertEquals("t@t.com", employees[0].getEmail());
        Assertions.assertEquals("Test Employee", employees[0].getName());
        Assertions.assertEquals("Animator", employees[0].getJobTitle());
        Assertions.assertEquals("EMPLOYEE", employees[0].getRole());
        Assertions.assertEquals("EMAIL", employees[0].getNotificationType());
        Assertions.assertNull(employees[0].getPhoneNumber());
    }

    @Test
    @DisplayName("Unauthenticated user should get forbidden")
    public void testGetNonAuthenticatedUser() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
        mockMvc.perform(
                MockMvcRequestBuilders.get("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("Invalid user requests should get bad request responses")
    public void testCreateNewManagerWithInvalidBody() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "david@ex.com",
                                        "short",
                                        "Police Detective",
                                        null))))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "invalid email",
                                        "password",
                                        "Police Detective",
                                        null))))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "david@ex.com",
                                        "password",
                                        null,
                                        null))))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest(null,
                                        "david@ex.com",
                                        "password",
                                        "asd",
                                        null))))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create new employee")
    @Transactional
    public void testCreateNewEmployee() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "david@starsky.si",
                                        "password",
                                        "Police detective",
                                        UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d")))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
        Assertions.assertEquals("david@starsky.si", response.getEmail());
        Assertions.assertEquals("Police detective", response.getJobTitle());
        Assertions.assertEquals("David Starsky", response.getName());
        Assertions.assertEquals("EMAIL", response.getNotificationType());
        Assertions.assertEquals("EMPLOYEE", response.getRole());
        Assertions.assertNull(response.getPhoneNumber()); //atm phone numbers are only supported in database, but not when actually registering
    }

    @Test
    @DisplayName("Should get invite expired error")
    public void testCreateEmployeeWithExpiredInvite() throws Exception {
        var uuid = UUID.fromString("acaa86b2-ce32-4911-89b8-e1e2a1d39a09");
        var invite = Mockito.mock(Invite.class);
        when(invite.getHasRegistered()).thenReturn(false);
        when(invite.getUpdatedAt()).thenReturn(Instant.now().minus(Duration.ofDays(4)));
        when(invite.getManager()).thenReturn(Mockito.mock(User.class));
        when(inviteRepository.getIfAvailable().findByToken(uuid)).thenReturn(invite);

        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky",
                                        "david@starsky.io",
                                        "password",
                                        "Police detective",
                                        uuid))))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), InviteInvalidResponse.class);
        Assertions.assertEquals("Invite has expired - all invites have expiry date of 3 days.", response.getError());
        Assertions.assertEquals(uuid.toString(), response.getInviteToken());
    }

    @Test
    @DisplayName("Should get user has already registered error")
    public void testCreateAlreadyRegisteredEmployee() throws Exception {
        var uuid = UUID.fromString("acaa86b2-ce32-4911-89b8-e1e2a1d39a01");
        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("David Starsky", "david@starsky.ioo", "password", "Police detective", uuid))))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), InviteInvalidResponse.class);
        Assertions.assertEquals("Invite has already been used, user has already been registered.", response.getError());
        Assertions.assertEquals(uuid.toString(), response.getInviteToken());
    }

    @Test
    @DisplayName("Should send new invite")
    public void testSendNewInvite() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get existing invite")
    public void testGetInvite() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var invitesResponse = objectMapper.readValue(result.getResponse().getContentAsString(), InviteResponse[].class);
        Assertions.assertTrue(invitesResponse.length > 0);
        var invite = invitesResponse[0];
        Assertions.assertNotNull(invite);
        Assertions.assertNotNull(invite.getEmployeeEmail());
        Assertions.assertNotNull(invite.getEmployeeName());
        Assertions.assertNotNull(invite.getExpiresOn());

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/invites/%d".formatted(invite.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), InviteResponse.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(invite.getEmployeeEmail(), response.getEmployeeEmail());
        Assertions.assertEquals(invite.getEmployeeName(), response.getEmployeeName());
        Assertions.assertEquals(invite.getExpiresOn(), response.getExpiresOn());
        Assertions.assertEquals(invite.getId(), response.getId());
        Assertions.assertEquals(invite.getExpiresIn(), response.getExpiresIn());
        Assertions.assertEquals(invite.getHasRegistered(), response.getHasRegistered());
    }

    @Test
    @DisplayName("Should get no invite found")
    public void testGetNotFoundInvite() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/invites/123456789")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Invalid invite should get bad request ")
    public void testSendInviteBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "invalid mail"))))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest(null, "david2@mail.net"))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Unauthenticated invite routes should get forbidden")
    public void testSendUnauthenticatedInvite() throws Exception {
        // no auth header - should not be allowed to see these endpoints
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.get("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isForbidden());

        // auth header present, but authenticated user has employee role - should not be allowed to see these endpoints
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", employeeJwtHeader)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.get("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", employeeJwtHeader)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get invite with this email already exists conflict")
    public void testSendExistingMail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "a@a.com"))))
                .andDo(print())
                .andExpect(status().isConflict());
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader)
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "t@t.com"))))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should get manager's teams")
    public void testGetManagerTeams() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", managerJwtHeader))
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
                .header("Authorization", employeeJwtHeader))
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
                .header("Authorization", managerJwtHeader))
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
                .header("Authorization", managerJwtHeader))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get conflict when creating a new team with existing name")
    public void testCreateExistingTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("Test Team")))
                .header("Authorization", managerJwtHeader))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Employee should get forbidden when creating a new team")
    public void testEmployeeCreateNewTeam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTeamRequest("new test team")))
                .header("Authorization", employeeJwtHeader))
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

    @TestConfiguration
    static class TestConfig {
        @Primary
        @Bean
        InviteRepository testBean(InviteRepository real) {
            return Mockito.mock(InviteRepository.class, AdditionalAnswers.delegatesTo(real));
        }
    }
}
