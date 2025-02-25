Feature: Trainer Workload Management

  Scenario: Add trainer hours to a new training
    Given there is a new training with trainer "JohnDoe"
    When I add 3 training hours for "JohnDoe" on "2021-09-15"
    Then the total training hours for "JohnDoe" in month 9 for year 2021 should be 3

  Scenario: Decrease training hours for an existing trainer
    Given there is an existing trainer "JaneDoe" with 5 training hours in month 10 of year 2021
    When I delete 2 training hours for "JaneDoe" on "2021-10-15"
    Then the total training hours for "JaneDoe" in month 10 for year 2021 should be 3

  Scenario: Accumulate training hours for the same month
    Given there is an existing trainer "JaneDoe" with 5 training hours in month 10 of year 2021
    When I add 10 training hours for "JaneDoe" on "2021-10-03"
    Then the total training hours for "JaneDoe" in month 10 for year 2021 should be 15

  Scenario: Attempt to get training hours when none exist
    Given there is a trainer "JohnSmith"
    When I request training hours for "JohnSmith" in month 11 for year 2021
    Then a "No workload data" exception should be thrown

  Scenario: Create a record for a new trainer upon first training session
    Given a trainer "EmilyRoe" does not exist
    When I add 4 training hours for "EmilyRoe" on "2021-12-01"
    Then a record should be created for "EmilyRoe" and the total hours in "12" for "2021" should be 4