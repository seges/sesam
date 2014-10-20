package sk.seges.sesam.core.pap.test.model.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;


public class TestDeclaredType extends TestTypeMirror implements DeclaredType {

	private TypeElement element;
	private TypeMirror[] typeArgs = null;
	
	public TestDeclaredType(TypeElement element) {
		super(TypeKind.DECLARED);
		this.element = element;
	}

	public TestDeclaredType(TypeElement element, TypeMirror... typeArgs) {
		super(TypeKind.DECLARED);
		this.element = element;
		this.typeArgs = typeArgs;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitDeclared(this, p);
	}

	@Override
	public Element asElement() {
		return element;
	}

	@Override
	public String toString() {
		return asElement().toString();
	}
	
	@Override
	public TypeMirror getEnclosingType() {
		Element enclosingElement = element.getEnclosingElement();
		
		if (enclosingElement == null) {
			return null;
		}
		
		return enclosingElement.asType();
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		if (typeArgs != null) {
			List<TypeMirror> args = new ArrayList<TypeMirror>();

			for (TypeMirror typeArg: typeArgs) {
				args.add(typeArg);
			}
			
			return args;
		}
		
		List<TypeMirror> parameters = new ArrayList<TypeMirror>();
		
		for (TypeParameterElement parameter: element.getTypeParameters()) {
			parameters.add(parameter.asType());
		}

		return parameters;
	}

    @Override
    protected Annotation[] getAnnotations() {
        return ((TestElement)element).getAnnotations();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return ((TestElement)element).getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return ((TestElement)element).getAnnotationsByType(annotationType);
    }
}