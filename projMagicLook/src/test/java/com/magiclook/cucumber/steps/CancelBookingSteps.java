package com.magiclook.cucumber.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CancelBookingSteps {
	private final CommonSteps commonSteps;

	public CancelBookingSteps(CommonSteps commonSteps) {
		this.commonSteps = commonSteps;
	}

	@After
	public void tearDown() {
		if (commonSteps.driver != null) {
			commonSteps.driver.quit();
		}
	}

	@When("she goes to Minhas reservas")
	public void she_goes_to_minhas_reservas() {
            WebElement myReservationsLink = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("navMinhasReservas")));
		myReservationsLink.click();
	}

	@And("she clicks on the reservation to cancel and accesses the reservation")
	public void she_clicks_on_reservation_to_cancel() {
		WebElement cardBody = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".card-body")));
		cardBody.click();
	}

	@And("she clicks on the cancel reservation button")
	public void she_clicks_cancel_reservation_button() {
		WebElement cancelBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("cancelButton")));
		cancelBtn.click();
	}

	@And("she confirms the cancellation")
	public void she_confirms_the_cancellation() {
		WebElement cancelModal = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cancelModal")));
		WebElement confirmBtn = cancelModal.findElement(By.id("confirmCancelButton"));
		confirmBtn.click();
	}

	@Then("she receives confirmation of the cancellation")
	public void she_receives_confirmation_of_cancellation() {
		commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//*[contains(text(), 'Reserva cancelada com sucesso.')]")
		));
	}
}
