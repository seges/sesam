package sk.seges.sesam.core.pap.configuration.api;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;

public interface ProcessorConfigurer {

	Set<MutableDeclaredType> getElements(RoundEnvironment roundEnvironment);
	
	boolean hasSupportedAnnotation(Element element);
	boolean isSupportedByInterface(TypeElement typeElement);
	
	void init(MutableProcessingEnvironment processingEnv, AbstractProcessor processor);
	
	void flushMessages(Messager messager, Element element);
	
	Set<String> getSupportedAnnotations();
	
	boolean isSupportedKind(ElementKind kind);
}