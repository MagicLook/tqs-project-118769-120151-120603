Functionality: Add Item
  As a staff member
  I want to add new items to the inventory
  So that customers can rent them

  Scenario: Successfully add a new item
    Given that Camila is on the website
    When she clicks to add an item
    And specifies the item's characteristics
    And submits
    Then she receives confirmation that the item has been added
