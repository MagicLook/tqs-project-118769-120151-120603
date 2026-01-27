package com.magiclook.cucumber.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BookingItemSteps {
    private final CommonSteps commonSteps;

    public BookingItemSteps(CommonSteps commonSteps) {
        this.commonSteps = commonSteps;
    }

    @After
    public void tearDown() {
        if (commonSteps.driver != null) {
            commonSteps.driver.quit();
        }
    }

    @And("she has already chosen the clothing")
    public void she_has_already_chosen_the_clothing() {
        WebElement firstItem = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".item-card")));
        firstItem.click();
    }

    @When("she clicks to reserve")
    public void she_clicks_to_reserve() {
        WebElement reserveBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-reserve")));
        reserveBtn.click();
    }

    @And("selects the desired reservation date")
    public void selects_the_desired_reservation_date() {
        WebElement dateInput = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reservationDate")));
        dateInput.sendKeys("2026-02-01");
    }

    @And("she confirms the reservation")
    public void she_confirms_the_reservation() {
        WebElement confirmBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("confirmReservationBtn")));
        confirmBtn.click();
    }

    @Then("she receives confirmation of the reservation")
    public void she_receives_confirmation_of_the_reservation() {
        WebElement successMsg = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Reserva efetuada com sucesso')]")));
        assert successMsg.isDisplayed();
    }

    @And("the reservation is available in the \"Minhas reservas\" section")
    public void reservation_is_available_in_my_reservations() {
        WebElement myReservationsLink = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("myReservationsLink")));
        myReservationsLink.click();
        WebElement reservation = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Reserva efetuada com sucesso')]")));
        assert reservation.isDisplayed();
    }
}
