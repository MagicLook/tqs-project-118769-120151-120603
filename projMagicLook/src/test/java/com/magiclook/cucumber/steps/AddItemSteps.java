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
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddItemSteps {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
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
        // Assuming the app is running locally on port 8080
        // You might need to change this if your test environment is different
        driver.get("http://localhost:8080/magiclook/staff/login");

        // Log in first as staff
        driver.findElement(By.id("usernameOrEmail")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify we are on dashboard or items page
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    @When("she clicks to add an item")
    public void she_clicks_to_add_an_item() {
        // Navigate to items page first if needed
        driver.get("http://localhost:8080/magiclook/staff/item");

        // Click the "Add Item" button
        // Assuming there is a button with ID or Class.
        // Based on typical bootstrap structure, let's assume a button for opening modal
        WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-bs-target='#createItemModal']")));
        addButton.click();
    }

    @And("specifies the item's characteristics")
    public void specifies_the_items_characteristics() {
        // Wait for modal to be visible
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createItemModal")));

        // Fill form fields
        modal.findElement(By.id("itemName")).sendKeys("Selenium Test Dress");
        modal.findElement(By.id("itemBrand")).sendKeys("TestBrand");
        modal.findElement(By.id("itemMaterial")).sendKeys("Silk");
        modal.findElement(By.id("itemColor")).sendKeys("Red");
        modal.findElement(By.id("itemSize")).sendKeys("M");
        modal.findElement(By.id("itemPriceRent")).sendKeys("50.0");
        modal.findElement(By.id("itemPriceSale")).sendKeys("150.0");

        // Select dropdowns if necessary (Category/Subcategory)
        // new
        // Select(modal.findElement(By.id("itemCategory"))).selectByVisibleText("Mulher");
    }

    @And("submits")
    public void submits() {
        // Click save button in the modal
        // Assuming the submit button is inside the form in the modal
        driver.findElement(By.cssSelector("#createItemModal button[type='submit']")).click();
    }

    @Then("she receives confirmation that the item has been added")
    public void she_receives_confirmation_that_the_item_has_been_added() {
        // Verify success message or redirection
        // Checking for a success alert or the item appearing in the list

        // Option A: Check URL or Alert
        // wait.until(ExpectedConditions.urlContains("success"));

        // Option B: Check if item is in the table
        // Reload page to be sure
        driver.get("http://localhost:8080/magiclook/staff/item");
        boolean itemFound = wait
                .until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Selenium Test Dress"));
        assertTrue(itemFound, "The added item was not found on the page.");
    }
}
