package com.starsky.backend.api.schedule.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.api.TestJwtProvider;
import com.starsky.backend.api.authentication.LoginRequest;
import com.starsky.backend.api.schedule.availability.EmployeeAvailabilityResponse;
import com.starsky.backend.api.schedule.shift.ScheduleShiftResponse;
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
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeAssignmentControllerTest extends TestJwtProvider {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private ScheduleShiftResponse shiftWithAssignments;
    private ScheduleShiftResponse shiftWithoutAssignments;
    private long employeeId;

    @BeforeAll
    void setup() throws Exception {
        setupLogins(mockMvc, objectMapper, new LoginRequest("scheduling@a.com", "password"), new LoginRequest("scheduling@1.com", "password"));

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        shiftWithAssignments = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse.class);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        shiftWithoutAssignments = objectMapper.readValue(result.getResponse().getContentAsString(), ScheduleShiftResponse.class);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/user/shifts/%d/availabilities".formatted(shiftWithAssignments.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponses = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAvailabilityResponse[].class);
        employeeId = availabilityResponses[0].getEmployeeId();
    }

    @Test
    public void managerShouldGetEmployeeAssignments() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAssignmentResponse[].class);
        Assertions.assertNotNull(availabilityResponse);
        Assertions.assertEquals(100, availabilityResponse.length);
    }

    @Test
    public void employeeShouldGetEmployeeAssignments() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        var availabilityResponse = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAssignmentResponse[].class);
        Assertions.assertNotNull(availabilityResponse);
        Assertions.assertEquals(100, availabilityResponse.length);
    }

    @Test
    public void shouldGetNotFoundEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1459054890/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldPutEmployeeAssignmentsToEmptyShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId()),
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 9, shiftWithoutAssignments.getId()),
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 10, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldPutBadRequestEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json")
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(null, shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), null, 8, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetNotFoundWhenPutEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 566565, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, 38348349)
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1498458945/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetUnprocessableEntityWhenPutEmployeeAssignments() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart().minusSeconds(60), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd().plusSeconds(60), 8, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart().minus(1, ChronoUnit.DAYS), shiftWithoutAssignments.getShiftEnd().plusSeconds(60), 8, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftEnd(), shiftWithoutAssignments.getShiftStart(), 8, shiftWithoutAssignments.getId())
                        }))
                .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftStart(), 8, shiftWithoutAssignments.getId())
                        }))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

//        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
//                        new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 8, shiftWithoutAssignments.getId()),
//                        new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 9, shiftWithoutAssignments.getId()),
//                        new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart().plusSeconds(60), shiftWithoutAssignments.getShiftEnd().minusSeconds(60), 10, shiftWithoutAssignments.getId()),
//                        new PutEmployeeAssignmentRequest(shiftWithoutAssignments.getShiftStart(), shiftWithoutAssignments.getShiftEnd(), 10, shiftWithoutAssignments.getId()),
//                }))
//                .header("Authorization", getManagerJwtHeader()))
//                .andDo(print())
//                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Transactional
    public void shouldPutEmployeeAssignmentsToAssignedShift() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/schedules/1/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PutEmployeeAssignmentRequest[]{
                                new PutEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart(), shiftWithAssignments.getShiftEnd(), 8, shiftWithAssignments.getId()),
                                new PutEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart(), shiftWithAssignments.getShiftEnd(), 9, shiftWithAssignments.getId()),
                                new PutEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart(), shiftWithAssignments.getShiftEnd(), 10, shiftWithAssignments.getId())
                        }))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    public void shouldDeleteEmployeeAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/schedules/1/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    public void shouldGetNotFoundWhenDeletingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/schedules/1/assignments/3498432789523478")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/schedules/14589054894589045/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void employeeShouldGetForbiddenWhenDeletingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/schedules/1/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void employeeShouldGetForbiddenWhenCreatingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/%d/employees/%d/assignments".formatted(shiftWithAssignments.getId(), employeeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getAssignment(shiftWithAssignments)))
                        .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldCreateAssignment() throws Exception {
        var assignment = getAssignment(shiftWithAssignments);
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/%d/employees/%d/assignments".formatted(shiftWithAssignments.getId(), employeeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignment))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAssignmentResponse.class);
        Assertions.assertEquals(assignment.getAssignmentStart(), response.getAssignmentStart());
        Assertions.assertEquals(assignment.getAssignmentEnd(), response.getAssignmentEnd());
        Assertions.assertEquals(employeeId, response.getEmployeeId());
        Assertions.assertEquals(shiftWithAssignments.getId(), response.getShiftId());
    }

    @Test
    @Transactional
    public void shouldGetBadRequestWhenCreatingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/%d/employees/%d/assignments".formatted(shiftWithAssignments.getId(), employeeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("invalid json"))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void shouldGetNotFoundWhenCreatingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/56345234/employees/%d/assignments".formatted(employeeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getAssignment(shiftWithAssignments)))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/%d/employees/49085480945890/assignments".formatted(shiftWithAssignments.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getAssignment(shiftWithAssignments)))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldGetNotUnprocessableEntityWhenCreatingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/%d/employees/%d/assignments".formatted(shiftWithAssignments.getId(), employeeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart().minusSeconds(600), shiftWithAssignments.getShiftEnd())))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/%d/employees/%d/assignments".formatted(shiftWithAssignments.getId(), employeeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart(), shiftWithAssignments.getShiftEnd().plusSeconds(600))))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.post("/user/schedules/1/shifts/%d/employees/%d/assignments".formatted(shiftWithAssignments.getId(), employeeId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateEmployeeAssignmentRequest(shiftWithAssignments.getShiftEnd(), shiftWithAssignments.getShiftStart())))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    private CreateEmployeeAssignmentRequest getAssignment(ScheduleShiftResponse shift) {
        return new CreateEmployeeAssignmentRequest(shift.getShiftStart(), shift.getShiftEnd());
    }

    @Test
    @Transactional
    public void shouldUpdateEmployeeAssignment() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart().plusSeconds(60), shiftWithAssignments.getShiftEnd().minusSeconds(60))))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeAssignmentResponse.class);
        Assertions.assertEquals(shiftWithAssignments.getShiftStart().plusSeconds(60), response.getAssignmentStart());
        Assertions.assertEquals(shiftWithAssignments.getShiftEnd().minusSeconds(60), response.getAssignmentEnd());
        Assertions.assertEquals(employeeId, response.getEmployeeId());
        Assertions.assertEquals(shiftWithAssignments.getId(), response.getShiftId());
    }

    @Test
    @Transactional
    public void shouldGetNotFoundWhenUpdatingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1/assignments/1453789435798543")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart().plusSeconds(60), shiftWithAssignments.getShiftEnd().minusSeconds(60))))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/15979856/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart().plusSeconds(60), shiftWithAssignments.getShiftEnd().minusSeconds(60))))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void employeeShouldGetForbiddenWhenUpdatingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart().plusSeconds(60), shiftWithAssignments.getShiftEnd().minusSeconds(60))))
                        .header("Authorization", getEmployeeJwtHeader()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void employeeShouldGetUnprocessableEntityWhenUpdatingAssignment() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeAssignmentRequest(shiftWithAssignments.getShiftStart().minusSeconds(60), shiftWithAssignments.getShiftEnd().plusSeconds(60))))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(MockMvcRequestBuilders.patch("/user/schedules/1/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmployeeAssignmentRequest(shiftWithAssignments.getShiftEnd(), shiftWithAssignments.getShiftStart())))
                        .header("Authorization", getManagerJwtHeader()))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }
}
