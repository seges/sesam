package sk.seges.sesam.core.pap.test.model.utils;

import sk.seges.sesam.core.pap.test.TestAnnotationMirror;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

abstract class TestElement extends TestAnnotatedConstruct implements Element {

	private ElementKind kind;
	
	protected TestElement(ElementKind kind) {
		this.kind = kind;
	}
	
	@Override
	public ElementKind getKind() {
		return kind;
	}

}