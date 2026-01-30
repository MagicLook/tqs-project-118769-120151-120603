@REQ_SCRUM-1
Feature: Epic 1: “Reserva de Roupa”

	@TEST_SCRUM-384 @REQ_SCRUM-5
	Scenario: Successfully book clothing
		Given that Alice is on the website
		And she has already chosen the clothing
		When she clicks to reserve
		And selects the desired reservation date
		And she confirms the reservation
		Then she receives confirmation of the reservation
		And the reservation is available in the "Minhas reservas" section
		
