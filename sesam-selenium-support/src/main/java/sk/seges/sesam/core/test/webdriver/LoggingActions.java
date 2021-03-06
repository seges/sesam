package sk.seges.sesam.core.test.webdriver;

import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import sk.seges.sesam.core.test.selenium.configuration.annotation.SeleniumSettings;
import sk.seges.sesam.core.test.webdriver.action.logging.LogginSendKeysAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingButtonReleaseAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingClickAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingClickAndHoldAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingContextClickAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingDoubleClickAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingKeyDownAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingKeyUpAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingMouseMoveAction;
import sk.seges.sesam.core.test.webdriver.action.logging.LoggingMoveToOffsetAction;
import sk.seges.sesam.core.test.webdriver.report.ActionsListener;

public class LoggingActions extends WebDriverActions {

	private final List<? extends ActionsListener> listeners;
	private final WebDriver webDriver;
	
	public LoggingActions(WebDriver webDriver, List<? extends ActionsListener> listeners, SeleniumSettings testEnvironment) {
		super(webDriver, testEnvironment);
		this.listeners = listeners;
		this.webDriver = webDriver;
	}

	public Actions click(WebElement onElement) {
		action.addAction(new LoggingClickAction(mouse, onElement, listeners, webDriver));
		return this;
	}

	public Actions doubleClick(WebElement onElement) {
		action.addAction(new LoggingDoubleClickAction(mouse, onElement, listeners, webDriver));
		return this;
	}
	
	public Actions keyDown(WebElement element, Keys theKey) {
		action.addAction(new LoggingKeyDownAction(keyboard, mouse, element, theKey, listeners, webDriver));
		return this;
	}

	public Actions keyUp(WebElement element, Keys theKey) {
		action.addAction(new LoggingKeyUpAction(keyboard, mouse, element, theKey, listeners, webDriver));
		return this;
	}
	
	public Actions sendKeys(WebElement element, CharSequence... keysToSend) {
		action.addAction(new LogginSendKeysAction(keyboard, mouse, element, keysToSend, listeners, webDriver));
		return this;
	}

	public Actions clickAndHold(WebElement onElement) {
		action.addAction(new LoggingClickAndHoldAction(mouse, onElement, listeners, webDriver));
		return this;
	}

	public Actions release(WebElement onElement) {
		action.addAction(new LoggingButtonReleaseAction(mouse, onElement, listeners, webDriver));
		return this;
	}

	public Actions moveToElement(WebElement toElement) {
		action.addAction(new LoggingMouseMoveAction(mouse, toElement, listeners, webDriver));
		return this;
	}

	public Actions moveToElement(WebElement toElement, int xOffset, int yOffset) {
		action.addAction(new LoggingMoveToOffsetAction(mouse, toElement, xOffset, yOffset, listeners, webDriver));
		return this;
	}
	
	public Actions moveByOffset(int xOffset, int yOffset) {
		action.addAction(new LoggingMoveToOffsetAction(mouse, null, xOffset, yOffset, listeners, webDriver));
		return this;
	}

	public Actions contextClick(WebElement onElement) {
		action.addAction(new LoggingContextClickAction(mouse, onElement, listeners, webDriver));
		return this;
	}

	public Actions dragAndDrop(WebElement source, WebElement target) {
		action.addAction(new LoggingClickAndHoldAction(mouse, source, listeners, webDriver));
		action.addAction(new LoggingMouseMoveAction(mouse, target, listeners, webDriver));
		action.addAction(new LoggingButtonReleaseAction(mouse, target, listeners, webDriver));
		return this;
	}

	public Actions dragAndDropBy(WebElement source, int xOffset, int yOffset) {
		action.addAction(new LoggingClickAndHoldAction(mouse, source, listeners, webDriver));
		action.addAction(new LoggingMoveToOffsetAction(mouse, null, xOffset, yOffset, listeners, webDriver));
		action.addAction(new LoggingButtonReleaseAction(mouse, null, listeners, webDriver));
		return this;
	}
}