package sk.seges.sesam.pap.model.accessor;

import sk.seges.sesam.core.pap.accessor.SingleAnnotationAccessor;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.Key;

import javax.lang.model.element.Element;

public class KeyAccessor extends SingleAnnotationAccessor<Key> {

	public KeyAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(element, Key.class, processingEnv);
	}

}
