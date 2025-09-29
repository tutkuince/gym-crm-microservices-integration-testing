package com.epam.gymcrm.cucumber.steps;

import com.epam.gymcrm.cucumber.World;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateTraineeProfileSteps {

    private final World world;
    @SuppressWarnings("unused")
    private final JdbcTemplate jdbc;

    public UpdateTraineeProfileSteps(World world, JdbcTemplate jdbc) {
        this.world = world;
        this.jdbc = jdbc;
    }

    private static final String PATH = "/api/v1/trainees";

    @When("I update trainee {string} profile to firstName {string}, lastName {string}, active {string}, dateOfBirth {string}, address {string}")
    public void i_update_trainee_profile(String username, String firstName, String lastName, String active, String dob, String address) {
        String body = """
                {
                  "username": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "isActive": %s,
                  "dateOfBirth": "%s",
                  "address": "%s"
                }
                """.formatted(username, firstName, lastName, active, dob, address);

        world.lastResponse =
                given()
                        .auth().oauth2(world.jwt)
                        .contentType(ContentType.JSON)
                        .body(body)
                        .put(PATH);
    }

    @When("I update trainee {string} profile to firstName {string}, lastName {string}, active {string}, dateOfBirth {string}, address {string} without auth")
    public void i_update_trainee_profile_without_auth(String username, String firstName, String lastName, String active, String dob, String address) {
        String body = """
                {
                  "username": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "isActive": %s,
                  "dateOfBirth": "%s",
                  "address": "%s"
                }
                """.formatted(username, firstName, lastName, active, dob, address);

        world.lastResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(body)
                        .put(PATH);
    }

    @Then("the trainee response should contain firstName {string}, lastName {string}, active {string}")
    public void the_trainee_response_should_contain(String firstName, String lastName, String active) {
        var res = world.lastResponse;
        assertThat(res.getStatusCode()).isIn(200, 201);

        var jp = res.jsonPath();
        String fn = getFirstNonBlank(jp, "firstName", "user.firstName", "data.firstName");
        String ln = getFirstNonBlank(jp, "lastName", "user.lastName", "data.lastName");
        Object activeVal = getFirstNonNull(jp, "isActive", "active", "user.isActive", "data.isActive");
        String act = activeVal == null ? null : String.valueOf(activeVal);

        String body = res.asString();
        if (fn == null) assertThat(body).contains(firstName); else assertThat(fn).isEqualTo(firstName);
        if (ln == null) assertThat(body).contains(lastName); else assertThat(ln).isEqualTo(lastName);
        if (act == null) assertThat(body).contains(active); else assertThat(act).isEqualTo(active);
    }

    private static String getFirstNonBlank(io.restassured.path.json.JsonPath jp, String... paths) {
        for (String p : paths) {
            try {
                String v = jp.getString(p);
                if (v != null && !v.isBlank()) return v;
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static Object getFirstNonNull(io.restassured.path.json.JsonPath jp, String... paths) {
        for (String p : paths) {
            try {
                Object v = jp.get(p);
                if (v != null) return v;
            } catch (Exception ignored) { }
        }
        return null;
    }
}
