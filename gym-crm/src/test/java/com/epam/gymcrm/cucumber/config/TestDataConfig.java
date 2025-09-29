package com.epam.gymcrm.cucumber.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

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

    @Bean
    ApplicationRunner seedOneTrainer(JdbcTemplate jdbc) {
        return args -> {
            // training type
            List<Long> typeIds = jdbc.queryForList("select id from training_types where training_type_name = ?", Long.class, "Cardio");
            Long typeId = typeIds.isEmpty() ? null : typeIds.getFirst();
            if (typeId == null) {
                jdbc.update("insert into training_types (training_type_name) values (?)", "Cardio");
                typeId = jdbc.queryForObject("select id from training_types where training_type_name = ?", Long.class, "Cardio");
            }
            // user
            String u = "mehmet.trainer";
            List<Long> uIds = jdbc.queryForList("select id from users where username = ?", Long.class, u);
            Long userId = uIds.isEmpty() ? null : uIds.getFirst();
            if (userId == null) {
                jdbc.update("""
          insert into users (first_name, last_name, username, password, is_active)
          values (?,?,?,?,?)
          """, "Mehmet", "Trainer", u, "{noop}dummy", true);
                userId = jdbc.queryForObject("select id from users where username = ?", Long.class, u);
            }
            // trainer
            Integer tCnt = jdbc.queryForObject("select count(1) from trainers where user_id = ?", Integer.class, userId);
            if (tCnt == null || tCnt == 0) {
                jdbc.update("insert into trainers (user_id, specialization_id) values (?,?)", userId, typeId);
            }
        };
    }

}