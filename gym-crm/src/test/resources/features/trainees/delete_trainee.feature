  # language: en
  Feature: Delete trainee by username

    Background:
      Given a username "admin" and a password "admin123"
      When I log in
      Then the response status should be 200
      And I should receive an access token

    Scenario: Happy path - trainee is deleted
      Given a trainee exists with username "ali.trainee"
      When I delete trainee "ali.trainee"
      Then the response status should be 200
      And trainee "ali.trainee" should not exist

    Scenario: Unknown trainee returns 404
      When I delete trainee "ghost.trainee"
      Then the response status should be 404

    Scenario: Unauthorized request
      Given a trainee exists with username "ayse.trainee"
      When I delete trainee "ayse.trainee" without auth
      Then the response status should be 401
