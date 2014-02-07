package sk.seges.sesam.pap.model.printer.constructor;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.accessor.ConstructorParameterAccessor;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;

public class ConstructorParameterHelper {

	private final MutableProcessingEnvironment processingEnv;

	public ConstructorParameterHelper(MutableProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	public boolean hasCustomerConstructorDefined(ConfigurationTypeElement configurationTypeElement) {

		//TODO handle superclass configuration also
		List<ExecutableElement> methods = ElementFilter.methodsIn(configurationTypeElement.asConfigurationElement().getEnclosedElements());

		for (ExecutableElement method: methods) {
			if (new ConstructorParameterAccessor(method, processingEnv).isValid()) {
				return true;
			}
		}

		return false;
	}
}
