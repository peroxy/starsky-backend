package com.starsky.backend.api.team;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.api.user.UserResponse;
import com.starsky.backend.domain.team.Team;
import com.starsky.backend.service.team.TeamService;
import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/user/teams", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Team", description = "Endpoints for user team management")
@SecurityRequirement(name = "bearerAuth")
public class TeamController extends BaseController {

    private final TeamService teamService;
    private final UserService userService;

    @Autowired
    public TeamController(TeamService teamService, UserService userService) {
        super(userService);
        this.teamService = teamService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get user's teams", description = "Returns the teams the user owns (manager) or the ones he is part of (employee).")
    @ApiResponse(responseCode = "200", description = "Response with a list of teams.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TeamResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated.", content = @Content)
    public ResponseEntity<TeamResponse[]> getTeams() {
        var user = getAuthenticatedUser();
        var teams = teamService.getTeams(user).stream().map(Team::toResponse).toArray(TeamResponse[]::new);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{team_id}/members")
    @Operation(summary = "Get team members", description = "Returns a list of team members of the specified team.")
    @ApiResponse(responseCode = "200", description = "Response with a list of team members.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team does not exist.", content = @Content)
    public ResponseEntity<UserResponse[]> getTeamMembers(@PathVariable("team_id") long teamId) {
        var user = getAuthenticatedUser();
        var teamMembers = teamService.getTeamMembers(teamId, user).stream().map(teamMember -> teamMember.getMember().toResponse()).toArray(UserResponse[]::new);
        return ResponseEntity.ok(teamMembers);
    }

    @PostMapping
    @Operation(summary = "Create a new team", description = "Create a new team - manager only route. Team name must be unique for this user, can't have 2 teams with same name.")
    @ApiResponse(responseCode = "200", description = "Response with the newly created team.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TeamResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Team name already exists.", content = @Content)
    public ResponseEntity<TeamResponse> postTeam(@Valid @RequestBody CreateTeamRequest request) {
        var user = getAuthenticatedUser();
        var team = teamService.createTeam(request.getName(), user);
        return ResponseEntity.ok(team.toResponse());
    }

    @PostMapping("/{team_id}/members/{user_id}")
    @Operation(summary = "Add a new team member", description = "Add a new team member (an employee with user ID) to a team - manager only route.")
    @ApiResponse(responseCode = "202", description = "Employee successfully added to the team.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team or employee does not exist.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Employee already present in the team.", content = @Content)
    public ResponseEntity<Void> postTeamMember(@PathVariable("team_id") long teamId, @PathVariable("user_id") long employeeId) {
        var manager = getAuthenticatedUser();
        var team = teamService.getTeam(teamId, manager);
        var employee = userService.getEmployeeById(employeeId, manager);
        var teamMember = teamService.createTeamMember(employee, team);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{team_id}/members")
    @Operation(summary = "Create or update team members", description = "Creates or updates team members. " +
            "Please note that this operation can be destructive - it will always delete all of the previous/existing team members (if they exist) for the specified team and create or update with the new ones. " +
            "Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Created/updated team members successfully.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team or employee does not exist.", content = @Content)
    public ResponseEntity<Void> putTeamMembers(@Valid @RequestBody List<CreateTeamMemberRequest> employees, @PathVariable(value = "team_id") long teamId) {
        var user = getAuthenticatedUser();
        teamService.putAll(employees, teamId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{team_id}")
    @Operation(summary = "Delete team", description = "Deletes the team. This will also cascade delete team members." +
            " Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Deleted the team successfully.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team does not exist.", content = @Content)
    public ResponseEntity<Void> deleteTeam(@PathVariable("team_id") long teamId) {
        var user = getAuthenticatedUser();
        teamService.deleteTeam(teamId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{team_id}/members/{user_id}")
    @Operation(summary = "Delete team member", description = "Deletes the team member from the team. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Deleted the team member successfully.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team or team member does not exist.", content = @Content)
    public ResponseEntity<Void> deleteTeamMember(@PathVariable("team_id") long teamId, @PathVariable("user_id") long employeeId) {
        var user = getAuthenticatedUser();
        teamService.deleteTeamMember(teamId, employeeId, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{team_id}")
    @Operation(summary = "Update team", description = "Update the specified team. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Updated the team successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TeamResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team does not exist.", content = @Content)
    public ResponseEntity<TeamResponse> patchTeam(@PathVariable("team_id") long teamId, @Valid @RequestBody UpdateTeamRequest request) {
        var user = getAuthenticatedUser();
        var team = teamService.updateTeam(teamId, request, user);
        return ResponseEntity.ok(team.toResponse());
    }

}
