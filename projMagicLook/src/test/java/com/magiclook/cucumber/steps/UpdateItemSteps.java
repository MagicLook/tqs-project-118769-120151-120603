package com.magiclook.cucumber.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateItemSteps {
	private final CommonSteps commonSteps;
	private String updatedItemName = "Vestido Atualizado Cucumber";

	public UpdateItemSteps(CommonSteps commonSteps) {
		this.commonSteps = commonSteps;
	}

	@After
	public void tearDown() {
		if (commonSteps.driver != null) {
			commonSteps.driver.quit();
		}
	}
    
	@When("she goes to the Roupa section")
	public void she_goes_to_roupa_section() {
		assertTrue(commonSteps.driver.getCurrentUrl().contains("staff/item"), "URL should contain 'staff/item'");
	}


	@And("she clicks on the edit button on an item that she wants to change")
	public void she_clicks_on_edit_button_on_item() {
		WebElement itemCard = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(
			By.cssSelector(".item-card")
		));
		WebElement alterarDadosBtn = itemCard.findElement(By.xpath(".//button[contains(.,'Alterar Dados')] | .//a[contains(.,'Alterar Dados')]"));
		alterarDadosBtn.click();
	}

	@And("fills in the details to be updated")
	public void fills_in_details_to_update() {
		WebElement editModal = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editItemModal")));
		WebElement nameInput = editModal.findElement(By.id("editName"));
		nameInput.clear();
		nameInput.sendKeys(updatedItemName);
	}

	@And("she confirms the update")
	public void she_confirms_the_update() {
		WebElement submitBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("editSubmitBtn")));
		submitBtn.click();
	}

	@Then("she receives confirmation and the item is updated")
	public void she_receives_confirmation_and_item_updated() {
		commonSteps.wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("editItemModal")));
		WebElement updatedItem = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//*[contains(text(), '" + updatedItemName + "')]")));
		assertTrue(updatedItem.isDisplayed(), "Updated item name should be visible on the page");
	}
}
