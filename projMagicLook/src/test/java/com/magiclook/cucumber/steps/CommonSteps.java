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

    public CommonSteps() {}

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
        driver.get("http://localhost:8080/magiclook/user/login");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameOrEmail")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-submit"));
        usernameField.sendKeys("alice");
        passwordField.sendKeys("alice123");
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/user/item"));
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
}
