@REQ_SCRUM-2
Feature: Epic 2: “Gestão de Itens”

	@TEST_SCRUM-383 @REQ_SCRUM-8
	Scenario: Successfully add a new item
		Given that Camila is on the website
		When she clicks to add an item
		And specifies the item characteristics
		And submits the form
		Then she receives confirmation that the item has been added
		
