package com.starsky.backend.api.invite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.authentication.TokenResponse;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.User;
import com.starsky.backend.service.invite.InviteService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InviteEndpointsTest {

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
    @DisplayName("Should get bad request")
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
    @DisplayName("Should get forbidden")
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
    @DisplayName("Should get email already exists conflict")
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

}
