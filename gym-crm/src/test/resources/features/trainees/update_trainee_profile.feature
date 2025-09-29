  # language: en
  Feature: Update trainee profile

    Background:
      Given a username "admin" and a password "admin123"
      When I log in
      Then the response status should be 200
      And I should receive an access token

    Scenario: Happy path - update names, active, dob, address
      Given a trainee exists with username "ali.trainee"
      When I update trainee "ali.trainee" profile to firstName "Ali", lastName "Yılmaz", active "true", dateOfBirth "1995-06-15", address "Ankara Çankaya"
      Then the response status should be 200
      And the trainee response should contain firstName "Ali", lastName "Yılmaz", active "true"

    Scenario: Unknown trainee returns 404
      When I update trainee "ghost.trainee" profile to firstName "X", lastName "Y", active "true", dateOfBirth "1990-01-01", address "Nope"
      Then the response status should be 404

    Scenario: Invalid date format returns 400
      Given a trainee exists with username "veli.trainee"
      When I update trainee "veli.trainee" profile to firstName "Veli", lastName "Kaya", active "false", dateOfBirth "15-06-1995", address "İzmir"
      Then the response status should be 400

    Scenario: Unauthorized request
      Given a trainee exists with username "ayse.trainee"
      When I update trainee "ayse.trainee" profile to firstName "Ayşe", lastName "Demir", active "true", dateOfBirth "1992-02-02", address "Kadıköy" without auth
      Then the response status should be 401
