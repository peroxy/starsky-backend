package com.starsky.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.config.JwtConfig;
import com.starsky.backend.service.authentication.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private final UserDetailsServiceImpl userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtConfig jwtConfig;
    private final ObjectMapper mapper;
    private final String frontendOrigin;

    @Autowired
    public WebSecurity(UserDetailsServiceImpl userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder, JwtConfig jwtConfig, ObjectMapper mapper,
                       @Value("${starsky.frontend.register-url}") String frontendRegisterUrl) throws MalformedURLException {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtConfig = jwtConfig;
        this.mapper = mapper;
        var url = new URL(frontendRegisterUrl);
        this.frontendOrigin = url.getProtocol() + "://" + url.getAuthority();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()

                /* PERMIT ALL PUBLIC ACCESS */
                .antMatchers(HttpMethod.POST, jwtConfig.getRegisterUrl(), "/login").permitAll()
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/version").permitAll()

                /* MANAGER ONLY ROUTES */
                .antMatchers(HttpMethod.POST,
                        "/user/invites",
                        "/user/teams/{teamId}/members/{userId}",
                        "/user/teams",
                        "/user/teams/{team_id}/schedules",
                        "/user/schedules/{schedule_id}/shifts",
                        "/user/shifts/{shift_id}/availabilities"
                ).hasRole("MANAGER")
                .antMatchers(HttpMethod.PATCH,
                        "/user/schedules/{schedule_id}",
                        "/user/shifts/{shift_id}",
                        "/user/availabilities/{availability_id}",
                        "/user/teams/{team_id}"
                ).hasRole("MANAGER")
                .antMatchers(HttpMethod.DELETE,
                        "/user/schedules/{schedule_id}",
                        "/user/shifts/{shift_id}",
                        "/user/availabilities/{availability_id}",
                        "/user/invites/{invite_id}",
                        "/user/teams/{team_id}",
                        "/user/teams/{team_id}/members/{user_id}"
                ).hasRole("MANAGER")
                .antMatchers(HttpMethod.GET,
                        "/user/invites",
                        "/user/employees",
                        "/user/schedules/{schedule_id}/solve"
                ).hasRole("MANAGER")
                .antMatchers(HttpMethod.PUT,
                        "/user/schedules/{schedule_id}/assignments",
                        "/user/teams/{team_id}/members"
                ).hasRole("MANAGER")


                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtConfig, mapper))
                .addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtConfig))
                // this disables session creation on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        var configuration = new CorsConfiguration();

        // allow all http methods
        configuration.addAllowedMethod("*");
        // allow all headers
        configuration.addAllowedHeader("*");
        // max age of 30 minutes
        configuration.setMaxAge(Duration.ofMinutes(30));
        // only allow access from frontend
        configuration.setAllowedOrigins(Collections.singletonList(frontendOrigin));

        // register the above configuration for all resources
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
