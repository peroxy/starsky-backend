package com.starsky.backend.api.team;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.api.user.UserResponse;
import com.starsky.backend.domain.Team;
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
        var teamMembers = teamService.getTeamMembers(teamId).stream().map(teamMember -> teamMember.getMember().toResponse()).toArray(UserResponse[]::new);
        return ResponseEntity.ok(teamMembers);
    }

    @PostMapping
    @Operation(summary = "Create a new team", description = "Create a new team - manager only route. Team name must be unique for this user, can't have 2 teams with same name.")
    @ApiResponse(responseCode = "200", description = "Response with the newly created team.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TeamResponse.class))))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Team name already exists.", content = @Content)
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        var user = getAuthenticatedUser();
        var team = teamService.createTeam(request.getName(), user);
        return ResponseEntity.ok(team.toResponse());
    }

    @PostMapping("/{team_id}/members/{user_id}")
    @Operation(summary = "Add a new team member", description = "Add a new team member (an employee with user ID) to a team - manager only route.")
    @ApiResponse(responseCode = "200", description = "Employee successfully added to the team.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team or employee does not exist.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Employee already present in the team.", content = @Content)
    public ResponseEntity<Void> createTeamMember(@PathVariable("team_id") long teamId, @PathVariable("user_id") long employeeId) {
        var manager = getAuthenticatedUser();
        var team = teamService.getTeam(teamId);
        var employee = userService.getEmployeeById(employeeId, manager);
        var teamMember = teamService.createTeamMember(employee, team);
        return ResponseEntity.ok().build();
    }
}
