package sk.seges.sesam.core.pap.test.model.utils;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;

abstract class TestNoType extends TestTypeMirror implements NoType {

	TestNoType(TypeKind kind) {
		super(kind);
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitNoType(this, p);
	}

    @Override
    protected Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return null;
    }
}
