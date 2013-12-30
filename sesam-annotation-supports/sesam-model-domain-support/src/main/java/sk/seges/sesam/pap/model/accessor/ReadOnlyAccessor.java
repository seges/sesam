package sk.seges.sesam.pap.model.accessor;

import javax.lang.model.element.Element;

import sk.seges.sesam.core.pap.accessor.SingleAnnotationAccessor;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.ReadOnly;

public class ReadOnlyAccessor extends SingleAnnotationAccessor<ReadOnly> {

	public ReadOnlyAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(element, ReadOnly.class, processingEnv);
	}

	public ReadOnly.PropertyType getPropertyType() {
		if (isValid()) {
			return annotation.value();
		}

		return null;
	}

	public boolean isReadonly() {
		return isValid();
	}
}