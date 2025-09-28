# language: en
Feature: User login

  Scenario: Login with valid credentials
    Given a username "admin" and a password "admin123"
    When I log in
    Then the response status should be 200
    And I should receive an access token

  Scenario: Login with wrong password
    Given a username "admin" and a password "wrong"
    When I log in
    Then the response status should be 401
