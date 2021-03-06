package sk.seges.sesam.pap.configuration;

import org.junit.Test;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.test.AnnotationTest;
import sk.seges.sesam.pap.configuration.api.TestConfiguration;
import sk.seges.sesam.pap.configuration.model.setting.SettingsTypeElement;
import sk.seges.sesam.pap.configuration.processor.SettingsProcessor;

import javax.annotation.processing.Processor;
import java.io.File;

public class ConfigurationProcessorTest extends AnnotationTest {

	@Test
	public void testConfiguration() {
		compileFiles(Compiler.JAVAC, TestConfiguration.class);
		assertOutput(getResourceFile(TestConfiguration.class), getOutputFile(TestConfiguration.class));
	}

    @Test
	public void testConfigurationInEclipse() {
		compileFiles(Compiler.ECLIPSE, TestConfiguration.class);
		assertOutput(getResourceFile(TestConfiguration.class), getOutputFile(TestConfiguration.class));
	}

	private File getOutputFile(Class<?> clazz) {
		MutableDeclaredType inputClass = toMutable(clazz);
		SettingsTypeElement outputClass = new SettingsTypeElement(inputClass, processingEnv);
		return new File(OUTPUT_DIRECTORY, toPath(outputClass.getPackageName()) + "/" + outputClass.getSimpleName() + SOURCE_FILE_SUFFIX);
	}

	@Override
	protected Processor[] getProcessors() {
		return new Processor[] {
			new SettingsProcessor()
		};
	}
}