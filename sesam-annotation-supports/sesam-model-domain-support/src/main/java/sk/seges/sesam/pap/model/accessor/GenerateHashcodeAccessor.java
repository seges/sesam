package sk.seges.sesam.pap.model.accessor;

import javax.lang.model.element.Element;

import sk.seges.sesam.core.pap.accessor.SingleAnnotationAccessor;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.GenerateEquals;
import sk.seges.sesam.pap.model.annotation.GenerateHashcode;
import sk.seges.sesam.pap.model.annotation.TraversalType;

public class GenerateHashcodeAccessor extends SingleAnnotationAccessor<GenerateHashcode> {

	private TraversalType type = TraversalType.CYCLIC_SAFE;

	public GenerateHashcodeAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(element, GenerateHashcode.class, processingEnv);

		if (annotation != null) {
			type = annotation.type();
		}
	}

	public TraversalType getType() {
		return type;
	}

	public boolean generate() {
		if (annotation != null) {
			return annotation.generate();
		}
		return true;
	}
}