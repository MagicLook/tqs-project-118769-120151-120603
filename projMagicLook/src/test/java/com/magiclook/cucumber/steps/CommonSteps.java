package com.magiclook.cucumber.steps;

import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.firefox.FirefoxDriver;
import java.time.Duration;

import io.cucumber.java.en.Given;

public class CommonSteps {
    public WebDriver driver;
    public WebDriverWait wait;

    @Before
    public void setup() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }
    public void setWait(WebDriverWait wait) {
        this.wait = wait;
    }

    @Given("that Alice is on the website")
    public void alice_is_on_the_website() {
        driver.get("http://localhost:8080/magiclook/login");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-submit"));
        usernameField.sendKeys("maria");
        passwordField.sendKeys("maria?");
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("magiclook/dashboard"));
    }

    @Given("that Camila is on the website")
    public void camila_is_on_the_website() {
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

    @Given("that Alice is on the Mulher page")
    public void alice_is_on_mulher_page() {
        driver.get("http://localhost:8080/magiclook/login");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-submit"));
        
        usernameField.sendKeys("maria");
        passwordField.sendKeys("maria?");
        
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("magiclook/dashboard"));
        
        WebElement mulherNav = wait.until(ExpectedConditions.elementToBeClickable(By.id("womenItemsLink")));
        mulherNav.click();

        wait.until(driver -> driver.getCurrentUrl().contains("items/women"));
        assertTrue(driver.getCurrentUrl().contains("items/women"), "URL should contain 'items/women'");
    }
}
