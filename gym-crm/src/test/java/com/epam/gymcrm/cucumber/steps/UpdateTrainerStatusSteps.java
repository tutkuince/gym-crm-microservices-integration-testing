package com.epam.gymcrm.cucumber.steps;

import com.epam.gymcrm.cucumber.World;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateTrainerStatusSteps {

    private final World world;
    private final JdbcTemplate jdbc;

    public UpdateTrainerStatusSteps(World world, JdbcTemplate jdbc) {
        this.world = world;
        this.jdbc = jdbc;
    }

    private static final String PATH = "/api/v1/trainers/status";

    /* ---------- API calls ---------- */

    @When("I set trainer {string} active status to {string}")
    public void i_set_trainer_active_status_to(String username, String active) {
        String body = """
                {
                  "username": "%s",
                  "isActive": %s
                }
                """.formatted(username, active);

        world.lastResponse =
                given()
                        .auth().oauth2(world.jwt)
                        .contentType(ContentType.JSON)
                        .body(body)
                        .patch(PATH);
    }

    @When("I set trainer {string} active status to {string} without auth")
    public void i_set_trainer_active_status_to_without_auth(String username, String active) {
        String body = """
                {
                  "username": "%s",
                  "isActive": %s
                }
                """.formatted(username, active);

        world.lastResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(body)
                        .patch(PATH);
    }

    /* ---------- DB helpers / assertions ---------- */

    @Given("a trainer exists with username {string}")
    public void a_trainer_exists_with_username(String username) {
        Long userId = jdbc.query(
                "SELECT id FROM users WHERE username = ?",
                rs -> rs.next() ? rs.getLong(1) : null,
                username
        );

        if (userId == null) {
            // ensure there is at least one training type
            Long ttId = jdbc.query(
                    "SELECT id FROM training_types LIMIT 1",
                    rs -> rs.next() ? rs.getLong(1) : null
            );
            if (ttId == null) {
                jdbc.update("INSERT INTO training_types (training_type_name) VALUES (?)", "General Fitness");
                ttId = jdbc.queryForObject("SELECT id FROM training_types WHERE training_type_name = ?",
                        Long.class, "General Fitness");
            }

            // create user
            jdbc.update("""
                    INSERT INTO users (first_name, last_name, username, password, is_active)
                    VALUES (?, ?, ?, ?, ?)
                    """, "Trainer", "User", username, "pwd", true);

            userId = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);

            // create trainer
            jdbc.update("INSERT INTO trainers (specialization_id, user_id) VALUES (?, ?)", ttId, userId);
        } else {
            // ensure trainer row exists for this user
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM trainers WHERE user_id = ?",
                    Integer.class, userId
            );
            if (Objects.requireNonNull(count) == 0) {
                Long ttId = jdbc.query(
                        "SELECT id FROM training_types LIMIT 1",
                        rs -> rs.next() ? rs.getLong(1) : null
                );
                if (ttId == null) {
                    jdbc.update("INSERT INTO training_types (training_type_name) VALUES (?)", "General Fitness");
                    ttId = jdbc.queryForObject("SELECT id FROM training_types WHERE training_type_name = ?",
                            Long.class, "General Fitness");
                }
                jdbc.update("INSERT INTO trainers (specialization_id, user_id) VALUES (?, ?)", ttId, userId);
            }
        }
    }

    @Given("trainer {string} is currently active {string}")
    public void trainer_is_currently_active(String username, String active) {
        int updated = jdbc.update("UPDATE users SET is_active = ? WHERE username = ?",
                Boolean.parseBoolean(active), username);
        assertThat(updated)
                .withFailMessage("User '%s' not found; call 'a trainer exists with username \"%s\"' first.", username, username)
                .isGreaterThan(0);
    }

    @Then("trainer {string} should be active {string}")
    public void trainer_should_be_active(String username, String expectedActive) {
        Boolean isActive = jdbc.queryForObject(
                "SELECT is_active FROM users WHERE username = ?",
                Boolean.class, username
        );
        assertThat(String.valueOf(isActive)).isEqualTo(expectedActive);
    }
}
