package com.epam.gymcrm.cucumber.steps;

import com.epam.gymcrm.cucumber.World;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private final World world;

    public LoginSteps(World world) {
        this.world = world;
    }

    private String username;
    private String password;

    // adjust if your endpoint differs:
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    @Given("a username {string} and a password {string}")
    public void a_username_and_a_password(String u, String p) {
        this.username = u;
        this.password = p;
    }

    @When("I log in")
    public void i_log_in() {
        world.lastResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password))
                        .post(LOGIN_PATH);
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int code) {
        assertThat(world.lastResponse.getStatusCode()).isEqualTo(code);
    }

    @Then("I should receive an access token")
    public void i_should_receive_an_access_token() {
        String token = extractToken(world.lastResponse.getBody().asString(),
                world.lastResponse.getHeaders().getValue("Authorization"),
                world.lastResponse.getHeaders().getValue("Set-Cookie"));

        if (token == null || token.isBlank()) {
            System.out.println("Login response body:\n" + world.lastResponse.getBody().asPrettyString());
            System.out.println("Login response headers:\n" + world.lastResponse.getHeaders());
        }

        assertThat(token)
                .as("Token should exist in JSON (accessToken/token/jwt/...) or in Authorization header / cookie")
                .isNotBlank();

        world.jwt = token;
    }

    // ---- helpers ----
    private static String extractToken(String body, String authHeader, String setCookie) {
        try {
            var json = io.restassured.path.json.JsonPath.from(body);
            for (String p : new String[]{
                    "accessToken", "token", "jwt", "jwtToken", "access_token",
                    "data.accessToken", "data.token", "data.jwt"
            }) {
                String v = json.getString(p);
                if (v != null && !v.isBlank()) return v;
            }
        } catch (Exception ignored) {
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
        }

        if (setCookie != null) {
            Matcher m = Pattern.compile("(?i)(jwt|token|access_token)=([^;]+)").matcher(setCookie);
            if (m.find()) return m.group(2);
        }
        return null;
    }
}
