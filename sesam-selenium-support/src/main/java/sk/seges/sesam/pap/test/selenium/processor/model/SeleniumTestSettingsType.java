package sk.seges.sesam.pap.test.selenium.processor.model;

import java.util.ArrayList;
import java.util.List;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.test.webdriver.configuration.DefaultTestSettings;

public class SeleniumTestSettingsType extends AbstractSeleniumType {

	public static final String SUFFIX = "Configuration";
	
	private SeleniumTestCaseType seleniumTestTypeElement;
	
	SeleniumTestSettingsType(SeleniumTestCaseType seleniumTestTypeElement, MutableProcessingEnvironment processingEnv) {
		super(processingEnv);
		this.seleniumTestTypeElement = seleniumTestTypeElement;
		
		setSuperClass(processingEnv.getTypeUtils().toMutableType(DefaultTestSettings.class));

		List<MutableTypeMirror> interfaces = new ArrayList<MutableTypeMirror>();
		for (SeleniumSuiteType seleniumSuite: seleniumTestTypeElement.getSeleniumSuites()) {
			interfaces.add(new SeleniumSettingsProviderTypeElement(seleniumSuite, processingEnv));
		}
		setInterfaces(interfaces);
	}
	
	@Override
	protected MutableDeclaredType getDelegate() {
		return seleniumTestTypeElement.clone().addClassSufix(SUFFIX);
	}
	
	public SeleniumTestCaseType getSeleniumTest() {
		return seleniumTestTypeElement;
	}
}