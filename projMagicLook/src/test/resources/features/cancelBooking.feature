@REQ-SCRUM-23
Feature: Cancel booking

    @TEST-SCRUM-23
    Scenario: Successfully cancels a booking
        Given that Alice is on the website
        And she has a reservation made
        When she goes to "Minhas reservas"
        And she clicks on the reservation to cancel and accesses the reservation
        And she clicks on the cancel reservation button
        And she confirms the cancellation
        Then she receives confirmation of the cancellation
        And she receives a refund depending on the day of cancellation in relation to the reservation date