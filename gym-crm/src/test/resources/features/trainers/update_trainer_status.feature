  # language: en
  Feature: Update trainer active status

    Background:
      Given a username "admin" and a password "admin123"
      When I log in
      Then the response status should be 200
      And I should receive an access token

    Scenario: Activate an inactive trainer
      Given a trainer exists with username "mehmet.trainer"
      And trainer "mehmet.trainer" is currently active "false"
      When I set trainer "mehmet.trainer" active status to "true"
      Then the response status should be 200
      And trainer "mehmet.trainer" should be active "true"

    Scenario: Deactivate an active trainer
      Given a trainer exists with username "ayse.trainer"
      And trainer "ayse.trainer" is currently active "true"
      When I set trainer "ayse.trainer" active status to "false"
      Then the response status should be 200
      And trainer "ayse.trainer" should be active "false"

    Scenario: Unknown trainer returns 404
      When I set trainer "ghost.trainer" active status to "true"
      Then the response status should be 404

    Scenario: Unauthorized request
      Given a trainer exists with username "ali.trainer"
      When I set trainer "ali.trainer" active status to "true" without auth
      Then the response status should be 401
