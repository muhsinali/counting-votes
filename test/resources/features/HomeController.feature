Feature: counting votes
  As a vote counter
  I want to see the counts for candidates within a given time frame
  So that I can announce the winner of the competition

  Scenario: Counting Votes accepts a vote
    Given I am counting votes
    When I receive a vote for candidate "A" from voter "A"
    And voter "A" has not voted before
    Then I register that vote and return a 201 response

  Scenario: Counting Votes only accepts 3 votes per user
    Given I am counting votes
    And I have received 3 votes for candidate "A" from voter "B"
    When I receive a vote for candidate "A" from voter "B"
    Then I return a 403 response
    And I do not register that vote from voter "B"

  Scenario: Counting Votes only accepts 3 votes per user regardless of candidate
    Given I am counting votes
    And I have received 2 votes for candidate "A" from voter "B"
    And I have received 1 votes for candidate "D" from voter "B"
    When I receive a vote for candidate "D" from voter "B"
    Then I return a 403 response
    And I do not register that vote from voter "B"

  Scenario: Counting Votes returns the voting results
    Given I am counting votes
    And I have received 20000000 votes for 4 candidates
    And the votes are split:
      | A | 8,000,000 |
      | B | 2,000,000 |
      | C | 6,000,000 |
      | D | 4,000,000 |

    When I receive a request for the overall result
    Then I return the correct result
    And the response time is under 1 second