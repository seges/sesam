package sk.seges.sesam.core.pap.test.model.utils;

import sk.seges.sesam.core.pap.test.TestAnnotationMirror;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

abstract class TestTypeMirror extends TestAnnotatedConstruct implements TypeMirror {

	protected TypeKind kind;
    private List<AnnotationMirror> annotationMirrors;

	protected TestTypeMirror(TypeKind kind) {
		this.kind = kind;
    }

	@Override
	public TypeKind getKind() {
		return kind;
	}

}