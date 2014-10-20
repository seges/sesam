package sk.seges.sesam.core.pap.test.model.utils;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import sk.seges.sesam.core.pap.utils.PAPReflectionUtils;

import java.lang.annotation.Annotation;

public class TestPrimitiveType extends TestTypeMirror implements PrimitiveType {

	
	public TestPrimitiveType(Class<?> clazz) {
		super(PAPReflectionUtils.toPrimitive(clazz));
	}
	
	public TestPrimitiveType(TypeKind kind) {
		super(kind);
	}

    @Override
    protected Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitPrimitive(this, p);
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
