package sk.seges.sesam.pap.test.selenium.processor;

import java.io.File;

import javax.annotation.processing.Processor;

import org.junit.Test;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.test.AnnotationTest;
import sk.seges.sesam.core.test.selenium.runner.MockSuite;
import sk.seges.sesam.pap.test.selenium.processor.model.SeleniumSuiteRunnerType;

public class SeleniumSuiteConfigurationProcessorTest extends AnnotationTest {

	@Test
	public void testSuiteConfiguration() {
		compileFiles(MockSuite.class);
		assertOutput(getResourceFile(MockSuite.class), getOutputFile(MockSuite.class));
	}

//	@Test
//	public void testSuiteConfigurationForEclipse() {
//		assertCompilationSuccessful(compileFiles(Compiler.ECLIPSE, MockSuite.class));
//		assertOutput(getResourceFile(MockSuite.class), getOutputFile(MockSuite.class));
//	}

	private File getOutputFile(Class<?> clazz) {
		final MutableDeclaredType outputClass = toMutable(clazz).addClassSufix(SeleniumSuiteRunnerType.SUFFIX);
		return new File(OUTPUT_DIRECTORY, toPath(outputClass.getPackageName()) + "/" + outputClass.getSimpleName() + SOURCE_FILE_SUFFIX);
	}

	@Override
	protected Processor[] getProcessors() {
		return new Processor[] {
			new SeleniumSuiteRunnerProcessor()
		};
	}
}