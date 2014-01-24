package sk.seges.sesam.pap.model.accessor;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.GenerateEquals;
import sk.seges.sesam.pap.model.annotation.TraversalType;

import javax.lang.model.element.Element;

public class GenerateEqualsAccessor extends AbstractGenerateAccessor<GenerateEquals> {

	private TraversalType type = TraversalType.CYCLIC_SAFE;

	public GenerateEqualsAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(element, processingEnv);

		if (annotation != null) {
			type = annotation.type();
		}
	}

	@Override
	protected Class<GenerateEquals> getAnnotationClass() {
		return GenerateEquals.class;
	}

	@Override
	protected boolean getGenerateValue(GenerateEquals annotation) {
		return annotation.generate();
	}

	public TraversalType getType() {
		return type;
	}

	@Override
	protected boolean getDefaultValue() {
		return true;
	}
}
