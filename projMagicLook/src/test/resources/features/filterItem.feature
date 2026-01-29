@REQ-SCRUM-16
Feature: Search clothing by category

    @TEST-SCRUM-16
    Scenario: Search by color
        Given that Alice is on the Mulher page
        When she searches for clothing by color
        Then she sees the clothing according to the selected color filter

    Scenario: Search by brand
        Given that Alice is on the Mulher page
        When she searches for clothing by brand
        Then she sees the clothing according to the selected brand filter

    Scenario: Search by type
        Given that Alice is on the Mulher page
        When she searches for clothing by type
        Then she sees the clothing according to the selected type filter

    Scenario: Search by size
        Given that Alice is on the Mulher page
        When she searches for clothing by size
        Then she sees the clothing according to the selected size filter

    Scenario: Search by material
        Given that Alice is on the Mulher page
        When she searches for clothing by material
        Then she sees the clothing according to the selected material filter

    Scenario: Search by price
        Given that Alice is on the Mulher page
        When she searches for clothing by price
        Then she sees the clothing according to the selected price filter

    Scenario: Search by city
        Given that Alice is on the Mulher page
        When she searches for clothing by city
        Then she sees the clothing according to the selected city filter