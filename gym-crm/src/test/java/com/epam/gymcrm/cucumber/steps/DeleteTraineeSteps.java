package com.epam.gymcrm.cucumber.steps;

import com.epam.gymcrm.cucumber.World;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteTraineeSteps {

    private final World world;
    private final JdbcTemplate jdbc;

    public DeleteTraineeSteps(World world, JdbcTemplate jdbc) {
        this.world = world;
        this.jdbc = jdbc;
    }

    private static final String PATH = "/api/v1/trainees";

    @When("I delete trainee {string}")
    public void i_delete_trainee(String username) {
        world.lastResponse =
                given()
                        .auth().oauth2(world.jwt)
                        .contentType(ContentType.JSON)
                        .delete(PATH + "/" + username);
    }

    @When("I delete trainee {string} without auth")
    public void i_delete_trainee_without_auth(String username) {
        world.lastResponse =
                given()
                        .contentType(ContentType.JSON)
                        .delete(PATH + "/" + username);
    }

    @Then("trainee {string} should not exist")
    public void trainee_should_not_exist(String username) {
        Integer cnt = jdbc.queryForObject("""
                SELECT COUNT(*) 
                FROM trainees t 
                JOIN users u ON u.id = t.user_id 
                WHERE u.username = ?
                """, Integer.class, username);
        assertThat(cnt).isNotNull();
        assertThat(cnt).as("Trainee row still exists for username=%s", username).isZero();
    }
}
