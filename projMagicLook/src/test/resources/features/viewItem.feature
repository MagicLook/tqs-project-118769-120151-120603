@REQ-SCRUM-2
Feature: View clothing

    @TEST-SCRUM-2
    Scenario: Successfully views clothing item
        Given that Camila is on the website
        When she searches for the clothing she wants to view
        And she clicks on the item
        Then information about each item of the clothing is displayed along with its status

    Scenario: Search clothing by name
        Given that Camila is on the website
        When she searches for clothing by name
        Then the clothing matching the name is displayed

    Scenario: Search clothing by state
        Given that Camila is on the website
        When she searches for clothing by state
        Then the clothing matching the selected state is displayed