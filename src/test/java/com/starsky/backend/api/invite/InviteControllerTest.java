package com.starsky.backend.api.invite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.domain.invite.Invite;
import com.starsky.backend.domain.user.User;
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
public class InviteControllerTest extends TestJwtProvider {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private InviteService inviteService;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("a@a.com", "password"), new LoginRequest("t@t.com", "password"));
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
                .header("Authorization", getManagerJwtHeader())
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldSendAndDeleteInvite() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader())
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "test1@test2.net"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var inviteResponse = objectMapper.readValue(result.getResponse().getContentAsString(), InviteResponse.class);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/invites/%d".formatted(inviteResponse.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void shouldGetNotFoundWhenDeletingNonExistentInvite() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/invites/545872983")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void employeeShouldGetForbiddenWhenDeleting() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/invites/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get existing invite")
    public void testGetInvite() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
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
                .header("Authorization", getManagerJwtHeader()))
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
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Invalid invite should get bad request ")
    public void testSendInviteBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader())
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "invalid mail"))))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader())
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
                .header("Authorization", getEmployeeJwtHeader())
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.get("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader())
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "david@mail.net"))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get invite with this email already exists conflict")
    public void testSendExistingMail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader())
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "a@a.com"))))
                .andDo(print())
                .andExpect(status().isConflict());
        mockMvc.perform(MockMvcRequestBuilders.post("/user/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader())
                .content(objectMapper.writeValueAsString(new CreateInviteRequest("David Starsky", "t@t.com"))))
                .andDo(print())
                .andExpect(status().isConflict());
    }


}
