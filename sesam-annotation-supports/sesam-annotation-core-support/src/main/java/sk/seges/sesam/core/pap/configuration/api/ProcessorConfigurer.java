package sk.seges.sesam.core.pap.configuration.api;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;

public interface ProcessorConfigurer {

	Set<Element> getElements(RoundEnvironment roundEnvironment);
	
	AnnotationMirror getSupportedAnnotationMirror(Element element);
	Annotation getSupportedAnnotation(Element element);

	AnnotationValue getAnnotationValueByReturnType(TypeElement returnType, AnnotationMirror annotationMirror);
	AnnotationValue getAnnotationValueByReturnType(Class<?> returnClass, AnnotationMirror annotationMirror);

	boolean isSupportedByInterface(TypeElement typeElement);
	
	void init(MutableProcessingEnvironment processingEnv, AbstractProcessor processor);
	
	void flushMessages(Messager messager, Element element);
	
	Set<String> getSupportedAnnotations();
	
	boolean isSupportedKind(ElementKind kind);
}