package com.starsky.backend.api.schedule.availability;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Employee availability", description = "Endpoints for schedule employee availability management")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeAvailabilityController extends BaseController {
    public EmployeeAvailabilityController(UserService userService) {
        super(userService);
    }
}
