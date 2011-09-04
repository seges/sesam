package sk.seges.sesam.core.test.selenium.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import sk.seges.sesam.core.pap.Constants;

@Target(ElementType.TYPE)
//This is not working in the eclipse with the source retention policy
//@Retention(RetentionPolicy.SOURCE)
public @interface ReportConfiguration {
	
	@Target(ElementType.ANNOTATION_TYPE)
	//This is not working in the eclipse with the source retention policy
	//@Retention(RetentionPolicy.SOURCE)
	public @interface ScreenshotConfiguration {
		
		boolean produceScreenshots() default false;

		String resultDirectory() default Constants.NULL;
		
		String screenshotsDirectory() default Constants.NULL;
	}
	
	ScreenshotConfiguration screenshotConfiguration() default @ScreenshotConfiguration;
}