@REQ-SCRUM-24
Feature: Update information of an item

    @TEST-SCRUM-24
    Scenario: Successfully updates item information
        Given that Camila is on the website
        When she goes to the Roupa section
        And she clicks on the edit button on an item that she wants to change
        And fills in the details to be updated
        And she confirms the update
        Then she receives confirmation and the item is updated
