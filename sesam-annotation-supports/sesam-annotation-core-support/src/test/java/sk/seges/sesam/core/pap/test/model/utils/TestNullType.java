package sk.seges.sesam.core.pap.test.model.utils;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;

public class TestNullType extends TestTypeMirror implements NullType {

	public TestNullType() {
		super(TypeKind.NULL);
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitNull(this, p);
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
