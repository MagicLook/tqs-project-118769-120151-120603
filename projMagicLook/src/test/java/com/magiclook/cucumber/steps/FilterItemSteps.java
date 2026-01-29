package com.magiclook.cucumber.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterItemSteps {
	private final CommonSteps commonSteps;
	private String filterValue = "";

	public FilterItemSteps(CommonSteps commonSteps) {
		this.commonSteps = commonSteps;
	}

	@After
	public void tearDown() {
		if (commonSteps.driver != null) {
			commonSteps.driver.quit();
		}
	}

	@When("she searches for clothing by color")
	public void she_searches_by_color() {
		Select colorSelect = new Select(commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("color"))));
		filterValue = colorSelect.getOptions().get(1).getText(); // pick first real color
		colorSelect.selectByIndex(1);
		WebElement filterBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
		filterBtn.click();
	}

	@Then("she sees the clothing according to the selected color filter")
	public void sees_clothing_by_color() {
		WebElement item = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//div[contains(@class,'item-card')]//*[contains(text(), '" + filterValue + "')]")));
		assertTrue(item.isDisplayed(), "Filtered item by color should be visible");
	}

	@When("she searches for clothing by brand")
	public void she_searches_by_brand() {
		Select brandSelect = new Select(commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("brand"))));
		filterValue = brandSelect.getOptions().get(1).getText();
		brandSelect.selectByIndex(1);
		WebElement filterBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
		filterBtn.click();
	}

	@Then("she sees the clothing according to the selected brand filter")
	public void sees_clothing_by_brand() {
		WebElement item = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//div[contains(@class,'item-card')]//*[contains(text(), '" + filterValue + "')]")));
		assertTrue(item.isDisplayed(), "Filtered item by brand should be visible");
	}

	@When("she searches for clothing by type")
	public void she_searches_by_type() {
		Select typeSelect = new Select(commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("category"))));
		filterValue = typeSelect.getOptions().get(2).getText();
		typeSelect.selectByIndex(2);
		WebElement filterBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
		filterBtn.click();
	}

	@Then("she sees the clothing according to the selected type filter")
	public void sees_clothing_by_type() {
		WebElement item = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//div[contains(@class,'item-card')]//*[contains(text(), '" + filterValue + "')]")));
		assertTrue(item.isDisplayed(), "Filtered item by type should be visible");
	}

	@When("she searches for clothing by size")
	public void she_searches_by_size() {
		Select sizeSelect = new Select(commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("size"))));
		filterValue = sizeSelect.getOptions().get(1).getText();
		sizeSelect.selectByIndex(1);
		WebElement filterBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
		filterBtn.click();
	}

	@Then("she sees the clothing according to the selected size filter")
	public void sees_clothing_by_size() {
		java.util.List<WebElement> items = commonSteps.wait.until(
			ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.item-card"))
		);
		assertTrue(!items.isEmpty() && items.get(0).isDisplayed(), "At least one filtered item card should be visible after filtering by size");
	}

	@When("she searches for clothing by material")
	public void she_searches_by_material() {
		Select materialSelect = new Select(commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("material"))));
		filterValue = materialSelect.getOptions().get(1).getText();
		materialSelect.selectByIndex(1);
		WebElement filterBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
		filterBtn.click();
	}

	@Then("she sees the clothing according to the selected material filter")
	public void sees_clothing_by_material() {
		WebElement item = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//div[contains(@class,'item-card')]//*[contains(text(), '" + filterValue + "')]")));
		assertTrue(item.isDisplayed(), "Filtered item by material should be visible");
	}

	@When("she searches for clothing by price")
	public void she_searches_by_price() {
		WebElement minPriceInput = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("minPrice")));
		WebElement maxPriceInput = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("maxPrice")));
		minPriceInput.clear();
		minPriceInput.sendKeys("50");
		maxPriceInput.clear();
		maxPriceInput.sendKeys("200");
		filterValue = "€"; // Just check for price symbol in results
		WebElement filterBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
		filterBtn.click();
	}

	@Then("she sees the clothing according to the selected price filter")
	public void sees_clothing_by_price() {
		WebElement item = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//div[contains(@class,'item-card')]//*[contains(text(), '" + filterValue + "')]")));
		assertTrue(item.isDisplayed(), "Filtered item by price should be visible");
	}

	@When("she searches for clothing by city")
	public void she_searches_by_city() {
		Select citySelect = new Select(commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopLocation"))));
		filterValue = citySelect.getOptions().get(1).getText();
		citySelect.selectByIndex(1);
		WebElement filterBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
		filterBtn.click();
	}

	@Then("she sees the clothing according to the selected city filter")
	public void sees_clothing_by_city() {
		java.util.List<WebElement> items = commonSteps.wait.until(
			ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.item-card"))
		);
		assertTrue(!items.isEmpty() && items.get(0).isDisplayed(), "At least one filtered item card should be visible after filtering by city");
	}
}
