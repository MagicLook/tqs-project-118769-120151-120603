@REQ_SCRUM-3
Feature: Epic 3: “Reporte de Danos”

	@TEST_SCRUM-393 @REQ_SCRUM-9
	Scenario: Successfully report a damaged item
		Given that Camila is on the website
		When she searches for the item
		And changes its status to Danificado, providing a description of the garment's condition,
		Then she receives confirmation of the operation
		
