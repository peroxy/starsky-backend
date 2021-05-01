package com.starsky.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.config.JwtConfig;
import com.starsky.backend.service.authentication.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private final UserDetailsServiceImpl userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtConfig jwtConfig;
    private final ObjectMapper mapper;

    @Autowired
    public WebSecurity(UserDetailsServiceImpl userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder, JwtConfig jwtConfig, ObjectMapper mapper) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtConfig = jwtConfig;
        this.mapper = mapper;
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
                        "/user/schedules/{schedule_id}/shifts"
                ).hasRole("MANAGER")
                .antMatchers(HttpMethod.PATCH,
                        "/user/schedules/{schedule_id}",
                        "/user/schedules/{schedule_id}/shifts"
                ).hasRole("MANAGER")
                .antMatchers(HttpMethod.DELETE,
                        "/user/schedules/{schedule_id}",
                        "/user/schedules/{schedule_id}/shifts"
                ).hasRole("MANAGER")
                .antMatchers(HttpMethod.GET,
                        "/user/invites",
                        "/user/employees"
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
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
