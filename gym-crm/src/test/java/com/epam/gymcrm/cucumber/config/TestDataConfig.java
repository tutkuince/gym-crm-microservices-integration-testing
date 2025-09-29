package com.epam.gymcrm.cucumber.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@Profile("test")
public class TestDataConfig {

    @Bean
    ApplicationRunner seedAdmin(JdbcTemplate jdbc) {
        return args -> {
            Integer cnt = jdbc.queryForObject(
                    "select count(1) from users where username = ?", Integer.class, "admin");
            if (cnt == null || cnt == 0) {
                var encoder = new BCryptPasswordEncoder();
                String hashed = encoder.encode("admin123");
                jdbc.update("""
            insert into users (first_name, last_name, username, password, is_active)
            values (?, ?, ?, ?, ?)
            """, "Admin", "User", "admin", hashed, true);
            }
        };
    }
}