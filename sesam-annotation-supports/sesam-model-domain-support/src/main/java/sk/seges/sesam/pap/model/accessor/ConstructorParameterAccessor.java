package sk.seges.sesam.pap.model.accessor;

import sk.seges.sesam.core.pap.accessor.AnnotationAccessor;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.ConstructorParameter;

import javax.lang.model.element.Element;

public class ConstructorParameterAccessor extends AnnotationAccessor {

	private ConstructorParameter constructorParameter;

	public ConstructorParameterAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(processingEnv);

		constructorParameter = getAnnotation(element, ConstructorParameter.class);
	}

	@Override
	public boolean isValid() {
		return constructorParameter != null;
	}

	public int getIndex() {
		if (!isValid()) {
			return -1;
		}

		return constructorParameter.value();
	}
}
