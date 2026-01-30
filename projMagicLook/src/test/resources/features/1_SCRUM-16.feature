@REQ_SCRUM-12
Feature: Epic 5: “Pesquisa de Roupas”

	@TEST_SCRUM-386 @REQ_SCRUM-16
	Scenario: Search by price
		Given that Alice is on the Mulher page
		When she searches for clothing by price
		Then she sees the clothing according to the selected price filter
		
	@TEST_SCRUM-387 @REQ_SCRUM-16
	Scenario: Search by material
		Given that Alice is on the Mulher page
		When she searches for clothing by material
		Then she sees the clothing according to the selected material filter
		
	@TEST_SCRUM-388 @REQ_SCRUM-16
	Scenario: Search by color
		Given that Alice is on the Mulher page
		When she searches for clothing by color
		Then she sees the clothing according to the selected color filter
		
	@TEST_SCRUM-390 @REQ_SCRUM-16
	Scenario: Search by size
		Given that Alice is on the Mulher page
		When she searches for clothing by size
		Then she sees the clothing according to the selected size filter
		
	@TEST_SCRUM-389 @REQ_SCRUM-16
	Scenario: Search by brand
		Given that Alice is on the Mulher page
		When she searches for clothing by brand
		Then she sees the clothing according to the selected brand filter
		
	@TEST_SCRUM-392 @REQ_SCRUM-16
	Scenario: Search by type
		Given that Alice is on the Mulher page
		When she searches for clothing by type
		Then she sees the clothing according to the selected type filter
		
	@TEST_SCRUM-391 @REQ_SCRUM-16
	Scenario: Search by city
		Given that Alice is on the Mulher page
		When she searches for clothing by city
		Then she sees the clothing according to the selected city filter
		
