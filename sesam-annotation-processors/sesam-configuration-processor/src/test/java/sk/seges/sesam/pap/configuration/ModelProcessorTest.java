package sk.seges.sesam.pap.configuration;

import org.junit.Test;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.test.AnnotationTest;
import sk.seges.sesam.pap.configuration.api.TestAnnotation;
import sk.seges.sesam.pap.configuration.model.parameter.ModelType;
import sk.seges.sesam.pap.configuration.processor.PojoAnnotationTransformerProcessor;

import javax.annotation.processing.Processor;
import java.io.File;

public class ModelProcessorTest extends AnnotationTest {

	@Test
	public void testConfiguration() {
		assertCompilationSuccessful(compileFiles(Compiler.JAVAC, TestAnnotation.class));
		assertOutput(getResourceFile(TestAnnotation.class), getOutputFile(TestAnnotation.class));
	}

    @Test
	public void testConfigurationInEclipse() {
		assertCompilationSuccessful(compileFiles(Compiler.ECLIPSE, TestAnnotation.class));
		assertOutput(getResourceFile(TestAnnotation.class), getOutputFile(TestAnnotation.class));
	}

	private File getOutputFile(Class<?> clazz) {
		MutableDeclaredType inputClass = toMutable(clazz);
		ModelType outputClass = new ModelType(inputClass);
		return new File(OUTPUT_DIRECTORY, toPath(outputClass.getPackageName()) + "/" + outputClass.getSimpleName() + SOURCE_FILE_SUFFIX);
	}

	@Override
	protected Processor[] getProcessors() {
		return new Processor[] {
			new PojoAnnotationTransformerProcessor()
		};
	}

}
