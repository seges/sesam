package sk.seges.sesam.pap.model.accessor;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.GenerateClone;

import javax.lang.model.element.Element;

public class GenerateCloneAccessor extends AbstractGenerateAccessor<GenerateClone> {

	public GenerateCloneAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(element, processingEnv);
	}

	@Override
	protected Class<GenerateClone> getAnnotationClass() {
		return GenerateClone.class;
	}

	@Override
	protected boolean getGenerateValue(GenerateClone annotation) {
		return annotation.generate();
	}

	@Override
	protected boolean getDefaultValue() {
		return false;
	}

}
