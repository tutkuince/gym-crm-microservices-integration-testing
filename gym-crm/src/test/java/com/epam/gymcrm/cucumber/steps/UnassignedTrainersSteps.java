package com.epam.gymcrm.cucumber.steps;

import com.epam.gymcrm.cucumber.World;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UnassignedTrainersSteps {

    private final World world;
    private final JdbcTemplate jdbc;

    public UnassignedTrainersSteps(World world, JdbcTemplate jdbc) {
        this.world = world;
        this.jdbc = jdbc;
    }

    private static final String PATH = "/api/v1/trainees/unassigned-trainers";

    @Given("a trainee exists with username {string}")
    @Transactional
    public void a_trainee_exists(String username) {
        // user yoksa oluştur
        Integer uCnt = jdbc.queryForObject("select count(1) from users where username = ?", Integer.class, username);
        if (uCnt == null || uCnt == 0) {
            jdbc.update("""
          insert into users (first_name, last_name, username, password, is_active)
          values (?,?,?,?,?)
          """, "Ali", "Trainee", username, "{noop}dummy", true);
        }
        Long userId = jdbc.queryForObject("select id from users where username = ?", Long.class, username);

        // trainee yoksa bağla
        Integer tCnt = jdbc.queryForObject("select count(1) from trainees where user_id = ?", Integer.class, userId);
        if (tCnt == null || tCnt == 0) {
            jdbc.update("""
          insert into trainees (user_id, date_of_birth, address)
          values (?,?,?)
          """, userId, Date.valueOf(LocalDate.of(2000, 1, 1)), "Test Address");
        }
    }

    @When("I request unassigned trainers for {string}")
    public void i_request_unassigned(String traineeUsername) {
        world.lastResponse =
                given()
                        .auth().oauth2(world.jwt)
                        .accept(ContentType.JSON)
                        .queryParam("username", traineeUsername) // controller param adı
                        .get(PATH);
    }

    @When("I request unassigned trainers for {string} without auth")
    public void i_request_unassigned_without_auth(String traineeUsername) {
        world.lastResponse =
                given()
                        .accept(ContentType.JSON)
                        .queryParam("username", traineeUsername)
                        .get(PATH);
    }

    @Then("the response should contain an array of trainers")
    @Then("the response should be a JSON array")
    public void the_response_should_be_a_json_array_alias() {
        Response res = world.lastResponse;
        assertThat(res.getStatusCode()).isIn(200, 201);

        List<?> top = null;
        try { top = res.jsonPath().getList("$"); } catch (Exception ignored) {}
        if (top != null) {
            assertThat(top).isNotNull();
            return;
        }

        List<?> arr = null;
        for (String key : new String[] { "trainers", "unassignedActiveTrainers", "items", "data" }) {
            try {
                List<?> candidate = res.jsonPath().getList(key);
                if (candidate != null) { arr = candidate; break; }
            } catch (Exception ignored) {}
        }
        assertThat(arr).as("Should contain a trainers array in response").isNotNull();
    }
}
