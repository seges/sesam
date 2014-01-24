package sk.seges.sesam.pap.model.accessor;

import sk.seges.sesam.core.pap.accessor.AnnotationAccessor;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.annotation.GenerateClone;
import sk.seges.sesam.pap.model.annotation.TraversalType;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public abstract class AbstractGenerateAccessor<T extends Annotation> extends AnnotationAccessor {

	protected Boolean generate = null;
	protected T annotation;

	public AbstractGenerateAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(processingEnv);

		annotation = getAnnotation(element, getAnnotationClass());

		if (annotation != null) {
			generate = getGenerateValue(annotation);
		} else {
			generate = getDefaultValue();
		}
	}

	protected abstract Class<T> getAnnotationClass();
	protected abstract boolean getGenerateValue(T annotation);
	protected abstract boolean getDefaultValue();

	@Override
	public boolean isValid() {
		return generate != null;
	}

	public boolean generate() {
		return generate;
	}

}
