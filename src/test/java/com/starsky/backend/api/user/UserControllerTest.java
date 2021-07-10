package com.starsky.backend.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.domain.invite.Invite;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.InviteRepository;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest extends TestJwtProvider {

    @Autowired
    ObjectProvider<InviteRepository> inviteRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("a@a.com", "password"), new LoginRequest("t@t.com", "password"));
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
        Assertions.assertFalse(response.isManuallyAdded());
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
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var userResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
        Assertions.assertEquals("a@a.com", userResponse.getEmail());
        Assertions.assertEquals("Test Manager", userResponse.getName());
        Assertions.assertEquals("Test Manager", userResponse.getJobTitle());
        Assertions.assertEquals("MANAGER", userResponse.getRole());
        Assertions.assertEquals("EMAIL", userResponse.getNotificationType());
        Assertions.assertFalse(userResponse.isManuallyAdded());
        Assertions.assertNull(userResponse.getPhoneNumber());
    }

    @Test
    @Transactional
    public void shouldUpdateUser() throws Exception {
        var request = new UpdateUserRequest("New name", "new@email.com", "new password", "new job title");
        var result = mockMvc.perform(
                MockMvcRequestBuilders.patch("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var userResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
        Assertions.assertEquals(request.getEmail().get(), userResponse.getEmail());
        Assertions.assertEquals(request.getName().get(), userResponse.getName());
        Assertions.assertEquals(request.getJobTitle().get(), userResponse.getJobTitle());
        Assertions.assertEquals("MANAGER", userResponse.getRole());
        Assertions.assertEquals("EMAIL", userResponse.getNotificationType());
        Assertions.assertFalse(userResponse.isManuallyAdded());
        Assertions.assertNull(userResponse.getPhoneNumber());
    }

    @Test
    public void shouldGetBadRequestWhenUpdatingUser() throws Exception {
        var request = new UpdateUserRequest("New name", "invalid email", "new password", "new job title");
        mockMvc.perform(MockMvcRequestBuilders.patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        request = new UpdateUserRequest("   ", "mail@asd.com", "new password", "new job title");
        mockMvc.perform(MockMvcRequestBuilders.patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        request = new UpdateUserRequest("my name", null, "short", "new job title");
        mockMvc.perform(MockMvcRequestBuilders.patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        request = new UpdateUserRequest("my name", null, "my new password", "");
        mockMvc.perform(MockMvcRequestBuilders.patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNonAuthenticatedUserGetForbiddenWhenUpdatingUser() throws Exception {
        var request = new UpdateUserRequest("New name", "invalid email", "new password", "new job title");
        mockMvc.perform(MockMvcRequestBuilders.patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldGetConflictWhenUpdatingUserWithExistentEmail() throws Exception {
        var request = new UpdateUserRequest(null, "t@t.com", null, null);
        mockMvc.perform(MockMvcRequestBuilders.patch("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isConflict());
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
    @DisplayName("Should create new employee from invite token")
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
        Assertions.assertFalse(response.isManuallyAdded());
        Assertions.assertNull(response.getPhoneNumber()); //atm phone numbers are only supported in database, but not when actually registering
    }

    @Test
    @Transactional
    public void testCreateNewManualEmployee() throws Exception {
        var result = mockMvc.perform(
                MockMvcRequestBuilders.post("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("David Starsky",
                                        "david@s.io",
                                        "Employee Numero Uno")))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
        Assertions.assertEquals("david@s.io", response.getEmail());
        Assertions.assertEquals("Employee Numero Uno", response.getJobTitle());
        Assertions.assertEquals("David Starsky", response.getName());
        Assertions.assertEquals("EMAIL", response.getNotificationType());
        Assertions.assertEquals("EMPLOYEE", response.getRole());
        Assertions.assertTrue(response.isManuallyAdded());
        Assertions.assertNull(response.getPhoneNumber()); //atm phone numbers are only supported in database, but not when actually registering
    }

    @Test
    @Transactional
    public void shouldGetBadRequestWhenCreatingManualEmployee() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("",
                                        "david@s.io",
                                        "Employee Numero Uno")))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("asd",
                                        "david",
                                        "Employee Numero Uno")))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("asd",
                                        "david@david.com",
                                        null)))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetConflictWhenCreatingManualEmployeeWithExistingEmail() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("asd",
                                        "a@a.com",
                                        "Employee Numero Uno")))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    public void nonAuthenticatedUserShouldGetAccessDeniedWhenCreatingEmployee() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("David Starsky",
                                        "david@s.io",
                                        "Employee Numero Uno"))))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/user/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateEmployeeRequest("David Starsky",
                                        "david@s.io",
                                        "Employee Numero Uno")))
                        .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
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

    @TestConfiguration
    static class TestConfig {
        @Primary
        @Bean
        InviteRepository testBean(InviteRepository real) {
            return Mockito.mock(InviteRepository.class, AdditionalAnswers.delegatesTo(real));
        }
    }


}
