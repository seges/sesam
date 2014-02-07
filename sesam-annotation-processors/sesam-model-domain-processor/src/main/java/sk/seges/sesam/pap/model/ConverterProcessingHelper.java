package sk.seges.sesam.pap.model;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.model.TransferObjectMappingAccessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ConverterProcessingHelper {

	public static boolean isConverterGenerated(Element typeElement, MutableProcessingEnvironment processingEnv) {
		TypeElement domain = new TransferObjectMappingAccessor(typeElement, processingEnv).getDomain();

		if (domain == null) {
			//if there no domain defined, converter does not make sense
			return false;
		}

		return (!domain.getModifiers().contains(Modifier.ABSTRACT));
	}
}
