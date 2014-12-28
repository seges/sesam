package sk.seges.sesam.core.pap.test.cases;

import org.junit.Test;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.test.AnnotationTest;
import sk.seges.sesam.core.pap.test.cases.model.BasicModel;
import sk.seges.sesam.core.pap.test.cases.processor.FormattedOutputAnnotationProcessor;

import javax.annotation.processing.Processor;
import java.io.File;

public class FormattedOutputProcessorTestCase extends AnnotationTest {

	@Test
	public void testNestedBounds() {
		assertCompilationSuccessful(compileFiles(BasicModel.class));
		assertOutput(getResourceFile(null, BasicModel.class, ".formatted" + OUTPUT_FILE_SUFFIX), getOutputFile(BasicModel.class));
	}

	@Override
	protected Processor[] getProcessors() {
		return new Processor[] {
				new FormattedOutputAnnotationProcessor()
		};
	}
	
	private File getOutputFile(Class<?> clazz) {
		MutableDeclaredType outputClass = toMutable(clazz).addClassSufix(FormattedOutputAnnotationProcessor.SUFFIX);
		return new File(OUTPUT_DIRECTORY, toPath(outputClass.getPackageName()) + "/" + outputClass.getSimpleName() + SOURCE_FILE_SUFFIX);
	}
}
