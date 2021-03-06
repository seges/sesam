package sk.seges.sesam.core.pap.model.mutable.utils;

import sk.seges.sesam.core.pap.accessor.AnnotationAccessor;
import sk.seges.sesam.core.pap.model.api.HasAnnotations;
import sk.seges.sesam.core.pap.model.mutable.api.MutableAnnotationMirror;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.*;

class AnnotationHolderDelegate implements HasAnnotations {

	private Set<AnnotationMirror> annotations;
	private Set<MutableAnnotationMirror> mutableAnnotations;
	
	private final Element element;
	private final HasAnnotations hasAnnotations;
	private final MutableProcessingEnvironment processingEnv;
	
	AnnotationHolderDelegate(MutableProcessingEnvironment processingEnv) {
		this.element = null;
		this.hasAnnotations = null;
		this.processingEnv = processingEnv;
		this.annotations = new LinkedHashSet<AnnotationMirror>();
	}
	
	AnnotationHolderDelegate(MutableProcessingEnvironment processingEnv, Element element) {
		this.element = element;
		this.hasAnnotations = null;
		this.processingEnv = processingEnv;
	}

	AnnotationHolderDelegate(MutableProcessingEnvironment processingEnv, HasAnnotations hasAnnotations) {
		this.element = null;
		this.hasAnnotations = hasAnnotations;
		this.processingEnv = processingEnv;
	}

	public HasAnnotations annotateWith(AnnotationMirror annotation) {
		initializeAnnotations();
		annotations.add(annotation);
		return this;
	}

	private void copyAnnotations() {
		if (element != null) {
			for (AnnotationMirror annotation: element.getAnnotationMirrors()) {
				annotations.add(annotation);
			}
		}
		if (hasAnnotations != null && hasAnnotations.getAnnotations() != null) {
			for (AnnotationMirror annotation: hasAnnotations.getAnnotations()) {
				//TODO convert to mutable annotation
				annotations.add(annotation);
			}
		}
	}

	private void copyMutableAnnotations() {
		if (hasAnnotations != null && hasAnnotations.getMutableAnnotations() != null) {
			for (MutableAnnotationMirror annotation: hasAnnotations.getMutableAnnotations()) {
				mutableAnnotations.add(annotation);
			}
		}
	}

	void initializeAnnotations() {
		if (annotations == null) {
			annotations = new LinkedHashSet<AnnotationMirror>();
			copyAnnotations();
		}
	}

	void initializeMutableAnnotations() {
		if (mutableAnnotations == null) {
			mutableAnnotations = new LinkedHashSet<MutableAnnotationMirror>();
			copyMutableAnnotations();
		}
	}
	
	public HasAnnotations setAnnotations(AnnotationMirror ...annotations) {
		initializeAnnotations();
		this.annotations.clear();
		
		if (annotations != null) {
			for (AnnotationMirror annotation: annotations) {
				if (annotation != null) {
					annotateWith(annotation);
				}
			}
		}
		
		return this;
	}

	public HasAnnotations setAnnotations(MutableAnnotationMirror ...annotations) {
		initializeMutableAnnotations();
		this.mutableAnnotations.clear();
		if (annotations != null) {
			for (MutableAnnotationMirror annotation: annotations) {
				if (annotation != null) {
					annotateWith(annotation);
				}
			}
		}		
		return this;
	}

	public Set<AnnotationMirror> getAnnotations() {
		initializeAnnotations();
		return Collections.unmodifiableSet(annotations);
	};
	
	void clone(AnnotationHolderDelegate annotationHolderDelegate) {
		if (getAnnotations() != null) {
			annotationHolderDelegate.annotations = new LinkedHashSet<AnnotationMirror>();
			for (AnnotationMirror annotationMirror: getAnnotations()) {
				annotationHolderDelegate.annotations.add(annotationMirror);
			}
		}
	}

	private Map<Class<?>, Annotation> annotationsCache = new HashMap<Class<?>, Annotation>();

	public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
		if (annotationsCache.containsKey(annotationType)) {
			return (A) annotationsCache.get(annotationType);
		}

		A result = new AnnotationAccessor(processingEnv) {

			@Override
			public boolean isValid() {
				return false;
			}
    		
			
			public A getAnnotation() {
				return super.getAnnotation(AnnotationHolderDelegate.this, annotationType);
			}
    	}.getAnnotation();
		annotationsCache.put(annotationType, result);

		return result;
    }

	@Override
	public HasAnnotations annotateWith(MutableAnnotationMirror mutableAnnotationMirror) {
		initializeMutableAnnotations();
		mutableAnnotations.add(mutableAnnotationMirror);
		return this;
	}

	@Override
	public Set<MutableAnnotationMirror> getMutableAnnotations() {
		initializeMutableAnnotations();
		return Collections.unmodifiableSet(mutableAnnotations);
	}
}