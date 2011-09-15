package sk.seges.sesam.core.test.selenium.factory;

import org.openqa.selenium.WebDriver;

import sk.seges.sesam.core.test.selenium.configuration.annotation.SeleniumSettings;

public interface WebDriverFactory {

	WebDriver createSelenium(SeleniumSettings testEnvironment);

}