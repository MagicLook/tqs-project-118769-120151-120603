package com.magiclook.cucumber.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddItemSteps {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("that Camila is on the website")
    public void that_camila_is_on_the_website() {
        driver.get("http://localhost:8080/magiclook/staff/login");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameOrEmail")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-submit"));

        usernameField.sendKeys("admin");
        passwordField.sendKeys("admin123");

        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/staff/item"));
        assertTrue(driver.getCurrentUrl().contains("/staff/item"));
    }

    @When("she clicks to add an item")
    public void she_clicks_to_add_an_item() {
        WebElement addItemButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-add-item")));
        addItemButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addItemModal")));
        assertTrue(driver.findElement(By.id("addItemModal")).isDisplayed());
    }

    @And("specifies the item characteristics")
    public void specifies_the_items_characteristics() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("genderSelect")));
        
        Select genderSelect = new Select(driver.findElement(By.id("genderSelect")));
        genderSelect.selectByValue("F");  // or selectByValue("F")
        
        Select sizeSelect = new Select(driver.findElement(By.id("sizeSelect")));
        sizeSelect.selectByVisibleText("M");
        
        Select materialSelect = new Select(driver.findElement(By.id("materialSelect")));
        materialSelect.selectByVisibleText("Seda");

        Select categorySelect = new Select(driver.findElement(By.id("categorySelect")));
        categorySelect.selectByVisibleText("Vestido");

        Select subcategorySelect = new Select(driver.findElement(By.id("subcategorySelect")));
        subcategorySelect.selectByVisibleText("Médio");
        
        WebElement nameInput = driver.findElement(By.id("nameItem"));
        nameInput.clear();
        nameInput.sendKeys("Vestido Teste Cucumber");
        
        WebElement brandInput = driver.findElement(By.id("brandItem"));
        brandInput.clear();
        brandInput.sendKeys("TestBrand");
        
        WebElement colorInput = driver.findElement(By.id("colorItem"));
        colorInput.clear();
        colorInput.sendKeys("Azul");
        
        WebElement priceSaleInput = driver.findElement(By.id("priceSale"));
        priceSaleInput.clear();
        priceSaleInput.sendKeys("1000.00");
        
        WebElement priceRentInput = driver.findElement(By.id("priceRent"));
        priceRentInput.clear();
        priceRentInput.sendKeys("100.00");
    }

    @And("submits the form")
    public void submits() {
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn")));
        submitButton.click();
    }

    @Then("she receives confirmation that the item has been added")
    public void she_receives_confirmation_that_the_item_has_been_added() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("addItemModal")));
        
        String itemName = "Vestido Teste Cucumber";
        WebElement addedItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), '" + itemName + "')]")
        ));
        
        assertTrue(addedItem.isDisplayed(), "Item '" + itemName + "' should be visible on the page");
    }
}
