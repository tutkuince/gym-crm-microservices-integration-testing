package com.epam.gymcrm.cucumber;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class World {
    public String jwt;
    public Response lastResponse;
}
