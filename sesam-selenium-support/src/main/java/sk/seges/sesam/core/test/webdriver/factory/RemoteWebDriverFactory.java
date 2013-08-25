package sk.seges.sesam.core.test.webdriver.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import sk.seges.sesam.core.test.selenium.configuration.annotation.SeleniumSettings;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class RemoteWebDriverFactory implements WebDriverFactory {

	@Override
	public WebDriver createSelenium(SeleniumSettings testEnvironment) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setBrowserName(testEnvironment.getBrowser().toString());
	
		WebDriver driver = new RemoteWebDriver(capabilities);
		Selenium selenium = new DefaultSelenium(testEnvironment.getSeleniumServer(), testEnvironment.getSeleniumPort(), "*webdriver", testEnvironment.getTestURL());
		selenium.start(driver);
		
		return driver;
	}
	
	protected String extractHost(String host) {
		if (host.toLowerCase().startsWith("http://")) {
			return host.substring("http://".length());
		}
		if (host.toLowerCase().startsWith("https://")) {
			return host.substring("https://".length());
		}
		return host;
	}
}