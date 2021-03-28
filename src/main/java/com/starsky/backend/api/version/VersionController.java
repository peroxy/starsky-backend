package com.starsky.backend.api.version;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Version", description = "Endpoint for version information.")
public class VersionController {

    private final BuildProperties buildProperties;

    public VersionController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/version")
    @Operation(summary = "Get current version",
            description = "Return the current version of the API which is specified inside gradle.build file.")
    @ApiResponse(responseCode = "200", description = "Returns current API version.")
    public ResponseEntity<VersionResponse> getVersion(){
        return ResponseEntity.ok(new VersionResponse(buildProperties.getVersion()));
    }
}
