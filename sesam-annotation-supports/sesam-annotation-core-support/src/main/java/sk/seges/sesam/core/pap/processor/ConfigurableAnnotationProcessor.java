package sk.seges.sesam.core.pap.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.annotation.configuration.ProcessorConfiguration;
import sk.seges.sesam.core.pap.builder.ClassPathSourceUtils;
import sk.seges.sesam.core.pap.builder.ClassPathTypeUtils;
import sk.seges.sesam.core.pap.builder.api.ClassPathSources;
import sk.seges.sesam.core.pap.builder.api.ClassPathTypes;
import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.utils.ListUtils;

public abstract class ConfigurableAnnotationProcessor extends PlugableAnnotationProcessor {

	public static final String SUPPORTED_PACKAGE = "sk.seges";
	
	public static final String PROJECT_NAME_OPTION = "projectName";
	public static final String CLASSPATH_OPTION = "classpath";
	public static final String TEST_CLASSPATH_OPTION = "testClasspath";

	public enum ExecutionType {
		ONCE, PER_TYPE
	}
	
	protected ProcessorConfigurer configurer;
	protected RoundEnvironment roundEnv;

	private Set<MutableDeclaredType> processedElements = new HashSet<MutableDeclaredType>();
	private Set<MutableDeclaredType> waitingElements = new HashSet<MutableDeclaredType>();

	private ClassPathTypes classPathTypes;
	private ClassPathSources classPathSources;
	
	protected ConfigurableAnnotationProcessor() {
		configurer = getConfigurer();
	}

	protected abstract ProcessorConfigurer getConfigurer();

	public ExecutionType getExecutionType() {
		return ExecutionType.PER_TYPE;
	}
	
    @Override
	public Set<String> getSupportedOptions() {
    	SupportedOptions so = this.getClass().getAnnotation(SupportedOptions.class);
    	Set<String> result = new HashSet<String>();
    	if  (so != null) {
    	    result = arrayToSet(so.value());
    	}
    	
    	result.add(CLASSPATH_OPTION);
    	result.add(PROJECT_NAME_OPTION);
    	result.add(TEST_CLASSPATH_OPTION);
    	
    	return result;
    }

    protected static Set<String> arrayToSet(String[] array) {
		assert array != null;
		Set<String> set = new HashSet<String>(array.length);
		for (String s : array) {
			set.add(s);
		}
		return Collections.unmodifiableSet(set);
    }

    protected String getSupportedPackage() {
    	return SUPPORTED_PACKAGE;
    }
    
    protected ClassPathSources getClassPathSources() {
    	if (classPathSources != null) {
    		return classPathSources;
    	}

    	return classPathSources = new ClassPathSourceUtils(processingEnv, getSupportedPackage());
    }
    
    protected ClassPathTypes getClassPathTypes() {
    	if (classPathTypes != null) {
    		return classPathTypes;
    	}

	    String projectName = processingEnv.getOptions().get(PROJECT_NAME_OPTION);
	    
	    if (projectName == null) {
	    	projectName = getClass().getCanonicalName();
	    }

	    return classPathTypes = new ClassPathTypeUtils(processingEnv, projectName, getSupportedPackage());
    }

	@Override
	public Set<String> getSupportedAnnotationTypes() {

	    SupportedAnnotationTypes sat = this.getClass().getAnnotation(SupportedAnnotationTypes.class);

		Set<String> result;
		
		if (sat != null) {
			result = new HashSet<String>(super.getSupportedAnnotationTypes());
		} else {
			result = new HashSet<String>();
		}
		
		if (configurer != null) {
			result.addAll(configurer.getSupportedAnnotations());
		}

		if (result.size() == 0) {
		    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "No SupportedAnnotationTypes for " + this.getClass().getName() + ", returning an empty set.");
		}

		return result;
	}

	@Override
	public synchronized void init(ProcessingEnvironment pe) {
		super.init(pe);
		//this.configurer.init(processingEnv, this);
	}
	
	protected boolean supportProcessorChain() {
		//Return true in order to run other processors
		return true;
	}

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		this.roundEnv = roundEnv;
		if (!roundEnv.processingOver()) {
			Set<MutableDeclaredType> processingElements;

			if (configurer != null) {
				configurer = getConfigurer();
				configurer.init(processingEnv, this);
				processingElements = configurer.getElements(roundEnv);
			} else {
				processingElements = new HashSet<MutableDeclaredType>();

				for (String annotationType: getSupportedAnnotationTypes()) {
					if (!annotationType.equals(ProcessorConfiguration.class.getCanonicalName())) {
						TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(annotationType);
						if (typeElement != null) {
							Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(typeElement);
							for (Element element: elementsAnnotatedWith) {
								//TODO handle methods
								MutableDeclaredType mutableDeclaredType = (MutableDeclaredType) processingEnv.getTypeUtils().toMutableType(element.asType());

								if (mutableDeclaredType == null) {
									processingEnv.getMessager().printMessage(Kind.ERROR, "Unable to create mutable element from " + element.toString());
								}
								processingElements.add(mutableDeclaredType);
							}
						}
					}
				}
			}
			
			for (MutableDeclaredType waitingElement: waitingElements) {
				//TODO handle methods
				processingElements.add(waitingElement);
			}
			
			waitingElements.clear();
			
			List<MutableDeclaredType> result = new LinkedList<MutableDeclaredType>();

			for (MutableDeclaredType element: processingElements) {
				if (element.getKind().isDeclared()) {
					//Do not touch this
					//TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(element.toString());

					TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(element.getCanonicalName().toString());

					if (!typeElement.getSuperclass().getKind().equals(TypeKind.ERROR)) {
						result.add(element);
					} else {
						processingEnv.getMessager().printMessage(Kind.WARNING, "Skipping processing of the " + typeElement + " type because it has noncompilable superclass");
						waitingElements.add(element);
					}
				} else {
					result.add(element);
				}
			}
			
			if (result.size() == 0) {
				for (MutableDeclaredType element: processingElements) {
					result.add(element);
				}
			}

			if (processElements(result) == 0) {
				processElements(waitingElements);
				waitingElements.clear();
			}
		}

		getClassPathTypes().setTargetChanged();
		
		return !supportProcessorChain();
	}

	private int processElements(Collection<MutableDeclaredType> elements) {
		Map<String, Element> els = new HashMap<String, Element>();

		for (MutableDeclaredType element: elements) {
			els.put(element.getCanonicalName(), processingEnv.getElementUtils().getTypeElement(element.toString(ClassSerializer.CANONICAL, false)));
		}
		
		int processedElementCount = 0;
		
		for (MutableDeclaredType element: elements) {
			if (!ListUtils.containsElement(processedElements, element)) {
				Element el = els.get(element.getCanonicalName());

				if (el == null) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "Element " + element.toString() + " is not available for the processing!");
				} else {
					if (configurer == null || configurer.isSupportedKind(el.getKind())) {

						if (processedElements.size() == 0 || !getExecutionType().equals(ExecutionType.ONCE)) {
							processedElements.add(element);
							init(el, roundEnv);
							processedElementCount += processElement(element, roundEnv);
							configurer.flushMessages(processingEnv.getMessager(), el);
						}
					}
				}
			}
		}
		
		return processedElementCount;
	}
	
	protected void init(Element element, RoundEnvironment roundEnv) {};
	
	protected abstract int processElement(MutableDeclaredType element, RoundEnvironment roundEnv);
}