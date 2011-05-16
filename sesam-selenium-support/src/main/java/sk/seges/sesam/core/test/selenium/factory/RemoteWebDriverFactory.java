package sk.seges.sesam.core.test.selenium.factory;

import java.net.URL;

import org.openqa.selenium.SeleneseCommandExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import sk.seges.sesam.core.test.selenium.configuration.api.TestEnvironment;

public class RemoteWebDriverFactory implements WebDriverFactory {

	@Override
	public WebDriver createSelenium(TestEnvironment testEnvironment) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setBrowserName(testEnvironment.getBrowser());
		CommandExecutor executor;
		try {
			executor = new SeleneseCommandExecutor(
					new URL("http", extractHost(testEnvironment.getSeleniumEnvironment().getSeleniumHost()),testEnvironment.getSeleniumEnvironment().getSeleniumPort(), "/"), 
					new URL("http", extractHost(testEnvironment.getHost()), 80, "/"), capabilities);
		} catch (Exception ex) {
			throw new RuntimeException("Invalid test environment specified.", ex);
		}
		return new RemoteWebDriver(executor, capabilities);
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