package sk.seges.sesam.pap.core;

import java.io.File;
import java.util.ArrayList;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.test.AnnotationTest;
import sk.seges.sesam.pap.model.model.ConfigurationContext;
import sk.seges.sesam.pap.model.model.ConfigurationEnvironment;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.provider.ConfigurationCache;
import sk.seges.sesam.pap.model.provider.RoundEnvConfigurationProvider;

public abstract class DtoTestCase extends AnnotationTest {

	protected File getOutputFile(Class<?> clazz) {
		MutableDeclaredType inputClass = toMutable(clazz);
		
		TransferObjectProcessingEnvironment tope = new TransferObjectProcessingEnvironment(processingEnv, roundEnv, new ConfigurationCache(), getProcessors()[0].getClass(), new ArrayList<MutableDeclaredType>());

        ConfigurationEnvironment env = new ConfigurationEnvironment(tope, roundEnv, new ConfigurationCache());

        ConfigurationContext context = new ConfigurationContext(env);
        ConfigurationTypeElement configurationTypeElement = new ConfigurationTypeElement(processingEnv.getElementUtils().getTypeElement(inputClass.getCanonicalName()), tope.getEnvironmentContext(), context);
        context.addConfiguration(configurationTypeElement);
        DtoDeclaredType dto = configurationTypeElement.getDto();

		return new File(OUTPUT_DIRECTORY, toPath(dto.getPackageName()) + "/" + dto.getSimpleName() + SOURCE_FILE_SUFFIX);
	}
}