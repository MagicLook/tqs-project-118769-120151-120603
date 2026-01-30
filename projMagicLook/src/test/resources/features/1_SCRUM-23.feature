@REQ_SCRUM-1
Feature: Epic 1: “Reserva de Roupa”

	@TEST_SCRUM-385 @REQ_SCRUM-23
	Scenario: Successfully cancels a booking
		Given that Alice is on the website
		When she goes to Minhas reservas
		And she clicks on the reservation to cancel and accesses the reservation
		And she clicks on the cancel reservation button
		And she confirms the cancellation
		Then she receives confirmation of the cancellation
		
