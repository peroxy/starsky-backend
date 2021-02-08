package com.starsky.backend;

import com.starsky.backend.domain.User;
import com.starsky.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;

@SpringBootApplication
public class StarskyApplication {

    private final UserRepository userRepository;
    private final Environment environment;

    @Autowired
    public StarskyApplication(UserRepository userRepository, Environment environment) {
        this.userRepository = userRepository;
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(StarskyApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CommandLineRunner runner(){
        return args -> {
            if (Arrays.asList(environment.getActiveProfiles()).contains("dev")){
                userRepository.deleteAllInBatch();

                User[] users = {
                        new User("John Doe", "john@doe.com", bCryptPasswordEncoder().encode("password"), "School Manager"),
                        new User("Test User", "a@a.com", bCryptPasswordEncoder().encode("password"), "Test Manager")
                };

                userRepository.saveAll(Arrays.asList(users));
            }
        };
    }


}
