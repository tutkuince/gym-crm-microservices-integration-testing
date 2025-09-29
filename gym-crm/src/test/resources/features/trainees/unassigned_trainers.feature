  # language: en
  Feature: List unassigned trainers

    Background:
      Given a username "admin" and a password "admin123"
      When I log in
      Then the response status should be 200
      And I should receive an access token

    Scenario: Existing trainee
      Given a trainee exists with username "ali.trainee"
      When I request unassigned trainers for "ali.trainee"
      Then the response status should be 200
      And the response should contain an array of trainers

    Scenario: Unknown trainee
      When I request unassigned trainers for "ghost.trainee"
      Then the response status should be 404

    Scenario: Unauthorized request
      When I request unassigned trainers for "ali.trainee" without auth
      Then the response status should be 401
