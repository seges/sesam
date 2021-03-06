package sk.seges.sesam.core.test.webdriver.filter;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Function;

public class Filters {

	public static Function<WebDriver, List<WebElement>> filterNotVisible(List<WebElement> webElements) {
		return new FilterVisible(webElements, false);
	}

	public static Function<WebDriver, List<WebElement>> filterVisible(List<WebElement> webElements) {
		return new FilterVisible(webElements, true);
	}
}
