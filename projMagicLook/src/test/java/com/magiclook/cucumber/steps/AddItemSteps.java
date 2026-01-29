package com.magiclook.cucumber.steps;


import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddItemSteps {
    private final CommonSteps commonSteps;

    public AddItemSteps(CommonSteps commonSteps) {
        this.commonSteps = commonSteps;
    }

    @When("she clicks to add an item")
    public void she_clicks_to_add_an_item() {

        WebElement addItemButton = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-add-item")));
        addItemButton.click();

        commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addItemModal")));
        assertTrue(commonSteps.driver.findElement(By.id("addItemModal")).isDisplayed());
    }

    @And("specifies the item characteristics")
    public void specifies_the_items_characteristics() {
        commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("genderSelect")));
        
        Select genderSelect = new Select(commonSteps.driver.findElement(By.id("genderSelect")));
        genderSelect.selectByValue("F");  // or selectByValue("F")
        
        Select sizeSelect = new Select(commonSteps.driver.findElement(By.id("sizeSelect")));
        sizeSelect.selectByVisibleText("M");
        
        Select materialSelect = new Select(commonSteps.driver.findElement(By.id("materialSelect")));
        materialSelect.selectByVisibleText("Seda");

        Select categorySelect = new Select(commonSteps.driver.findElement(By.id("categorySelect")));
        categorySelect.selectByVisibleText("Vestido");

        Select subcategorySelect = new Select(commonSteps.driver.findElement(By.id("subcategorySelect")));
        subcategorySelect.selectByVisibleText("Médio");
        
        WebElement nameInput = commonSteps.driver.findElement(By.id("nameItem"));
        nameInput.clear();
        nameInput.sendKeys("Vestido Teste Cucumber");
        
        WebElement brandInput = commonSteps.driver.findElement(By.id("brandItem"));
        brandInput.clear();
        brandInput.sendKeys("TestBrand");
        
        WebElement colorInput = commonSteps.driver.findElement(By.id("colorItem"));
        colorInput.clear();
        colorInput.sendKeys("Azul");
        
        WebElement priceSaleInput = commonSteps.driver.findElement(By.id("priceSale"));
        priceSaleInput.clear();
        priceSaleInput.sendKeys("1000.00");
        
        WebElement priceRentInput = commonSteps.driver.findElement(By.id("priceRent"));
        priceRentInput.clear();
        priceRentInput.sendKeys("100.00");
    }

    @And("submits the form")
    public void submits() {
        WebElement submitButton = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn")));
        submitButton.click();
    }

    @Then("she receives confirmation that the item has been added")
    public void she_receives_confirmation_that_the_item_has_been_added() {
        commonSteps.wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("addItemModal")));
        
        String itemName = "Vestido Teste Cucumber";
        WebElement addedItem = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), '" + itemName + "')]")
        ));
        
        assertTrue(addedItem.isDisplayed(), "Item '" + itemName + "' should be visible on the page");
    }

    @After
    public void tearDown() {
        if (commonSteps.driver != null) {
            commonSteps.driver.quit();
        }
    }
}
