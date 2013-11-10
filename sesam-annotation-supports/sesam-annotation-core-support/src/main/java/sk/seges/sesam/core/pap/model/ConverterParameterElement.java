package sk.seges.sesam.core.pap.model;

import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;

public class ConverterParameterElement extends ParameterElement {

	public ConverterParameterElement(MutableTypeMirror type, String name, MutableType usage, PropagationType propagatedType, MutableProcessingEnvironment processingEnv) {
		super(type, name, usage, propagatedType, processingEnv);
	}

	@Override
	public boolean isConverter() {
		return true;
	}
}