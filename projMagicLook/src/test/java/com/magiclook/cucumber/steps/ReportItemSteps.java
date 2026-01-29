package com.magiclook.cucumber.steps;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReportItemSteps {
    private final CommonSteps commonSteps;

    public ReportItemSteps(CommonSteps commonSteps) {
        this.commonSteps = commonSteps;
    }

    @When("she searches for the item")
    public void she_searches_for_the_item() {
        WebElement firstItemCard = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".item-card")
        ));
        WebElement verDetalhesBtn = firstItemCard.findElement(By.xpath(".//button[contains(., 'Ver Detalhes')]"));
        verDetalhesBtn.click();
    }

    @When("changes its status to Danificado, providing a description of the garment's condition,")
    public void she_changes_status_to_damaged_and_provides_description() {
        WebElement firstEditarBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Editar')]")
        ));
        firstEditarBtn.click();
        commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editItemSingleModal")));
        WebElement stateSelect = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editSingleState")));
        Select select = new Select(stateSelect);
        select.selectByVisibleText("Danificado");

        WebElement descInput = commonSteps.driver.findElement(By.id("editDamageReason"));
        descInput.clear();
        descInput.sendKeys("Botão solto");

        WebElement submitBtn = commonSteps.driver.findElement(By.id("editSingleSubmitBtn"));
        submitBtn.click();
    }

    @Then("she receives confirmation of the operation")
    public void she_receives_confirmation_of_the_operation() {
        WebElement successMsg = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Dados atualizados com sucesso')]")));
        assertTrue(successMsg.isDisplayed());

        commonSteps.wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("editItemSingleModal")));

        WebElement stateLabel = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Danificado')]")));
        assertTrue(stateLabel.isDisplayed());
    }

    @After
    public void tearDown() {
        if (commonSteps.driver != null) {
            commonSteps.driver.quit();
        }
    }
}
