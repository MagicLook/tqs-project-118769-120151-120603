@REQ-SCRUM-9
Feature: Report damaged item

    @TEST-SCRUM-8
    Scenario: Successfully report a damaged item
        Given that Camila is on the website
        And finds a damaged item,
        When she searches for the item
        And changes its status to ”Danificado" ,providing a description of the garment's condition,
        Then she receives confirmation of the operation
        And the user is notified.