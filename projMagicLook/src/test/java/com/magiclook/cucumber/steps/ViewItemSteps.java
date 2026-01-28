package com.magiclook.cucumber.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.*;

import org.checkerframework.checker.units.qual.s;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class ViewItemSteps {
	private final CommonSteps commonSteps;
	private String searchedItemName;

	public ViewItemSteps(CommonSteps commonSteps) {
		this.commonSteps = commonSteps;
	}

	@After
	public void tearDown() {
		if (commonSteps.driver != null) {
			commonSteps.driver.quit();
		}
	}

	@When("she searches for the clothing she wants to view")
	public void she_searches_for_clothing_to_view() {
		java.util.List<WebElement> itemCards = commonSteps.wait.until(
			ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.item-card"))
		);
		assert !itemCards.isEmpty() : "No items found in staff item page.";
        searchedItemName = itemCards.get(0).findElement(By.cssSelector("h6, .item-name")).getText();
	}

	@And("she clicks on the item")
	public void she_clicks_on_the_item() {
		WebElement itemCard = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(
			By.xpath("//div[contains(@class,'item-card')]//*[contains(text(), '" + searchedItemName + "')]/ancestor::div[contains(@class,'item-card')]")));
		WebElement verDetalhesBtn = itemCard.findElement(By.xpath(".//button[contains(.,'Ver Detalhes')] | .//a[contains(.,'Ver Detalhes')]"));
		verDetalhesBtn.click();
	}

	@Then("information about each item of the clothing is displayed along with its status")
	public void info_about_each_item_displayed() {
		WebElement detailsHeader = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".staff-header, .item-detail-image, .item-info-table, .item-status-table")));
		assert detailsHeader.isDisplayed();
		WebElement statusElement = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".state-badge, .item-status, .status-cell, .badge")));
		assert statusElement.isDisplayed();
	}

	@When("she searches for clothing by name")
	public void she_searches_for_clothing_by_name() {
		WebElement searchInput = commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchInput")));		WebElement firstItemNameElem = commonSteps.wait.until(
			ExpectedConditions.visibilityOfElementLocated(By.id("itemNameText"))
		);
		searchedItemName = firstItemNameElem.getText().trim();
		searchInput.clear();
		searchInput.sendKeys(searchedItemName);
		WebElement searchBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("searchBtn")));
		searchBtn.click();
	}

	@Then("the clothing matching the name is displayed")
	public void clothing_matching_name_displayed() {
		commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'item-card')]//*[contains(text(), '" + searchedItemName + "')]")));
	}

	@When("she searches for clothing by state")
	public void she_searches_for_clothing_by_state() {
		Select stateFilter = new Select(commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stateFilter"))));
		stateFilter.selectByValue("AVAILABLE");
		WebElement searchBtn = commonSteps.wait.until(ExpectedConditions.elementToBeClickable(By.id("searchBtn")));
		searchBtn.click();
	}

	@Then("the clothing matching the selected state is displayed")
	public void clothing_matching_state_displayed() {
		commonSteps.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".item-card .state-badge, .item-card .item-status, .item-card .status-cell, .item-card .badge")));
	}
}
