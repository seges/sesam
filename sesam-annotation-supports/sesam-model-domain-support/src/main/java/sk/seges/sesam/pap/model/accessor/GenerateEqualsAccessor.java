package sk.seges.sesam.pap.model.accessor;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.GenerateEquals;

import javax.lang.model.element.Element;

public class GenerateEqualsAccessor extends AbstractGenerateAccessor<GenerateEquals> {

	public GenerateEqualsAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(element, processingEnv);
	}

	@Override
	protected Class<GenerateEquals> getAnnotationClass() {
		return GenerateEquals.class;
	}

	@Override
	protected boolean getGenerateValue(GenerateEquals annotation) {
		return annotation.generate();
	}

	@Override
	protected boolean getDefaultValue() {
		return true;
	}
}
