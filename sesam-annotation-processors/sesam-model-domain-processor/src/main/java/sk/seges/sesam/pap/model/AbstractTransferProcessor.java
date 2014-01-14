package sk.seges.sesam.pap.model;

import java.util.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.PathResolver;
import sk.seges.sesam.core.pap.model.PojoElement;
import sk.seges.sesam.core.pap.processor.MutableAnnotationProcessor;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.accessor.CopyAccessor;
import sk.seges.sesam.pap.model.annotation.Id;
import sk.seges.sesam.pap.model.annotation.Ignore;
import sk.seges.sesam.pap.model.annotation.Mapping;
import sk.seges.sesam.pap.model.annotation.Mapping.MappingType;
import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;
import sk.seges.sesam.pap.model.configurer.TrasferObjectProcessorConfigurer;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationContext;
import sk.seges.sesam.pap.model.model.ConfigurationEnvironment;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.printer.api.TransferObjectElementPrinter;
import sk.seges.sesam.pap.model.provider.ConfigurationCache;
import sk.seges.sesam.pap.model.provider.RoundEnvConfigurationProvider;
import sk.seges.sesam.pap.model.provider.TransferObjectProcessorContextProvider;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.model.resolver.DefaultEntityResolver;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;
import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

public abstract class AbstractTransferProcessor extends MutableAnnotationProcessor {

	protected TransferObjectProcessorContextProvider transferObjectContextProvider;
	
	protected TransferObjectProcessingEnvironment processingEnv;
	protected EnvironmentContext<TransferObjectProcessingEnvironment> environmentContext;
	
	protected EntityResolver getEntityResolver() {
		return new DefaultEntityResolver();
	}

	protected ConfigurationCache getConfigurationCache() {
		return new ConfigurationCache();
	}
	
	protected EnvironmentContext<TransferObjectProcessingEnvironment> getEnvironmentContext() {
		if (environmentContext == null) {
			ConfigurationEnvironment configurationEnv = new ConfigurationEnvironment(processingEnv, roundEnv, getConfigurationCache());
			environmentContext = configurationEnv.getEnvironmentContext();
			configurationEnv.setConfigurationProviders(getConfigurationProviders());
		}
		
		return environmentContext;
	}
	
	@Override
	protected ProcessorConfigurer getConfigurer() {
		return new TrasferObjectProcessorConfigurer();
	}
	
	protected boolean contains(List<String> fields, String fieldName) {
		return fields.contains(fieldName);
	}

	protected ConfigurationTypeElement getConfigurationElement(ProcessorContext context) {
		return getConfigurationTypeElement(context.getTypeElement());
	}

	protected ConfigurationTypeElement getConfigurationElement(RoundContext context) {
		return getConfigurationTypeElement(context.getTypeElement());
	}

	protected ConfigurationTypeElement getConfigurationTypeElement(TypeElement typeElement) {
		ConfigurationContext configurationContext = new ConfigurationContext(environmentContext.getConfigurationEnv());
		ConfigurationTypeElement configurationTypeElement = new ConfigurationTypeElement(typeElement, getEnvironmentContext(), configurationContext);
		configurationContext.addConfiguration(configurationTypeElement);
		
		return configurationTypeElement;
	}
		
	@Override
	protected void printAnnotations(ProcessorContext context) {
		
		FormattedPrintWriter pw = context.getPrintWriter();
		
		pw.println("@SuppressWarnings(\"serial\")");

		ConfigurationTypeElement configurationTypeElement = getConfigurationElement(context);
		
		pw.print("@", TransferObjectMapping.class, "(");

		pw.println("dtoClass = " + configurationTypeElement.getDto().getSimpleName() + ".class,");
		pw.println("		domainClassName = \"" + configurationTypeElement.getInstantiableDomainSpecified().getQualifiedName().toString() + "\", ");
		pw.println("		configurationClassName = \"" + context.getTypeElement().toString() + "\", ");
		pw.print("		generateConverter = false, generateDto = false");
		if (configurationTypeElement.getConverter() != null) {
			pw.println(", ");
			pw.print("		converterClassName = \"");
			pw.print(configurationTypeElement.getConverter().getCanonicalName());
			pw.print("\"");
		}
		
		pw.println(")");
		
		super.printAnnotations(context);
	}

	@Override
	protected void processElement(ProcessorContext context) {
		TypeElement element = context.getTypeElement();
		
		ConfigurationTypeElement configurationElement = getConfigurationElement(context);

		if (configurationElement.getDomain() == null) {
			processingEnv.getMessager().printMessage(Kind.WARNING, "Unable to find domain class reference for " + element.toString(), element);
			return;
		}

		MappingType mappingType = getConfigurationMappingType(element);

		for (TransferObjectElementPrinter elementPrinter: getElementPrinters(context.getPrintWriter())) {
			elementPrinter.initialize(configurationElement, context.getOutputType());
			processMethods(configurationElement, mappingType, elementPrinter);
		}
		
		printAdditionalMethods(context);
	}

	protected MappingType getConfigurationMappingType(Element element) {

		MappingType mappingType = MappingType.AUTOMATIC;
		Mapping mapping =  element.getAnnotation(Mapping.class);

		if (mapping != null) {
			mappingType = mapping.value();
		}

		return mappingType;
	}

	protected void printAdditionalMethods(ProcessorContext context) {};
	
	protected abstract TransferObjectElementPrinter[] getElementPrinters(FormattedPrintWriter pw);
	
	protected boolean checkPreconditions(ProcessorContext context, boolean alreadyExists) {
		return getConfigurationElement(context).getDelegateConfigurationTypeElement() == null;
	}

	protected TransferObjectProcessorContextProvider getProcessorContextProvider(TransferObjectProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
		return new TransferObjectProcessorContextProvider(getEnvironmentContext(), getEntityResolver());
	}

	@Override
	protected void init(Element element, RoundEnvironment roundEnv) {
		super.init(element, roundEnv);
		this.processingEnv = new TransferObjectProcessingEnvironment(getProcessingEnv(), roundEnv, getConfigurationCache(), getClass(), getProcessingEnv().getUsedTypes());
		this.processingEnv.setConfigurationProviders(getConfigurationProviders());
		this.transferObjectContextProvider = getProcessorContextProvider(processingEnv, roundEnv);
	}
	
	protected ConfigurationProvider[] getConfigurationProviders() {
		return new ConfigurationProvider[] {
				new RoundEnvConfigurationProvider(getEnvironmentContext())
		};
	}

	protected void processMethods(ConfigurationTypeElement configurationTypeElement, MappingType mappingType, TransferObjectElementPrinter printer) {

		List<TransferObjectContext> contexts = new LinkedList<TransferObjectContext>();
		
		List<ExecutableElement> overridenMethods = ElementFilter.methodsIn(configurationTypeElement.asConfigurationElement().getEnclosedElements());
		
		DomainDeclaredType domainTypeElement = configurationTypeElement.getInstantiableDomain();
		DomainDeclaredType processingElement = domainTypeElement;
	
		List<String> generated = new ArrayList<String>();

		for (ExecutableElement overridenMethod: overridenMethods) {
			
			Ignore ignoreAnnotation = overridenMethod.getAnnotation(Ignore.class);
			if (ignoreAnnotation != null) {

				String fieldName = TransferObjectHelper.getFieldPath(overridenMethod);
				ExecutableElement domainMethod = processingElement.getGetterMethod(fieldName);

				if (domainMethod == null) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] " + fieldName + " is not defined in " + configurationTypeElement.getInstantiableDomain() + ".", configurationTypeElement.asConfigurationElement());
					return;
				}

				if (getEntityResolver().isIdMethod(domainMethod)) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Id method can't be ignored. There should be an id method available for merging purposes.", configurationTypeElement.asConfigurationElement());
					return;
				}
				generated.add(fieldName);
			} else {
				CopyAccessor copyAccessor = new CopyAccessor(overridenMethod, processingEnv);
				
				if (copyAccessor.isMethodBodyCopied()) {
					generated.add(TransferObjectHelper.getFieldPath(overridenMethod));
				}				
			}
		}

		for (ExecutableElement overridenMethod: overridenMethods) {

			String fieldName = TransferObjectHelper.getFieldPath(overridenMethod);
			
			if (!contains(generated, fieldName)) {

				generated.add(fieldName);

				ExecutableElement domainMethod = domainTypeElement.getGetterMethod(fieldName);

				if (domainMethod == null) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find method " + overridenMethod.getSimpleName().toString() + 
							" in the domain class " + domainTypeElement.getCanonicalName(), configurationTypeElement.asConfigurationElement());
					continue;
				}

				TransferObjectContext context = transferObjectContextProvider.get(configurationTypeElement, Modifier.PUBLIC, overridenMethod, domainMethod, false);
				if (context == null) {
					continue;
				}
				contexts.add(context);

				PathResolver pathResolver = new PathResolver(fieldName);
				String currentPath = pathResolver.next();
				String fullPath = currentPath;
				
				if (pathResolver.hasNext()) {

					DomainDeclaredType currentElement = processingElement;
	
					while (pathResolver.hasNext()) {
						DomainType domainReference = currentElement.getDomainReference(getEntityResolver(), currentPath);

						if (domainReference != null && domainReference.getKind().isDeclared()) {
							currentElement = (DomainDeclaredType)domainReference;
								ExecutableElement nestedIdMethod = currentElement.getIdMethod(getEntityResolver());
	
								if (nestedIdMethod == null && getEntityResolver().shouldHaveIdMethod(currentElement)) {
									//TODO Check @Id annotation is the configuration - nested field names
									processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find id method in the class " + currentElement.getCanonicalName() +
											". If the class/interface does not have strictly specified ID, please specify the id in the configuration using " + 
											Id.class.getCanonicalName() + " annotation.", configurationTypeElement.asConfigurationElement());
								} else {
									if (nestedIdMethod != null) {
										String idPath = fullPath + "." + MethodHelper.toField(nestedIdMethod);
										if (!contains(generated, idPath)) {
											context = transferObjectContextProvider.get(configurationTypeElement, Modifier.PROTECTED, nestedIdMethod, nestedIdMethod, fullPath, false);
											if (context == null) {
												continue;
											}
											contexts.add(context);
											generated.add(idPath);
										}
									}
							}
						} else {
							if (pathResolver.hasNext()) {
								processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Invalid mapping specified in the field " + fieldName + ". Current path (" + 
										currentPath + ") address getter type that is not class/interfaces." +
										"You probably mistyped this field in the configuration.", configurationTypeElement.asConfigurationElement());
							}
						}
						currentPath = pathResolver.next();
						fullPath += "." + currentPath;
					}
				}
			}
		}

		HistoryExecutableElementsList methodsForProcessings = getMethodsForProcessings(configurationTypeElement, mappingType, processingElement, domainTypeElement);

		for (ExecutableElement method: methodsForProcessings) {
			TransferObjectContext context = createTOContext(method, domainTypeElement, false, configurationTypeElement, generated);
			if (context != null) {
				contexts.add(context);
			}
		}

		for (ExecutableElement superClassMethod: methodsForProcessings.getRemovedElements()) {
			TransferObjectContext context = createTOContext(superClassMethod, domainTypeElement, true, configurationTypeElement, generated);
			if (context != null) {
				contexts.add(context);
			}
		}

		ExecutableElement idMethod = domainTypeElement.getIdMethod(getEntityResolver());
		
		if (idMethod == null && getEntityResolver().shouldHaveIdMethod(domainTypeElement)) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Identifier method could not be found in the automatic way. Please specify id method using " + 
					Id.class.getSimpleName() + " annotation or just specify id as a method name.", configurationTypeElement.asConfigurationElement());
			return;
		} else if (idMethod != null && !contains(generated, MethodHelper.toField(idMethod))) {
			TransferObjectContext context = transferObjectContextProvider.get(configurationTypeElement, Modifier.PROTECTED, idMethod, idMethod, false);
			if (context != null) {
				contexts.add(context);
				generated.add(TransferObjectHelper.getFieldPath(idMethod));
			}
		}

		Collections.sort(contexts, new Comparator<TransferObjectContext>() {

			@Override
			public int compare(TransferObjectContext o1, TransferObjectContext o2) {
				return o1.getDtoFieldName().compareTo(o2.getDtoFieldName());
			}
		});
		
		printContexts(configurationTypeElement, contexts, generated, printer);
	}

	private TransferObjectContext createTOContext(ExecutableElement method, DomainDeclaredType domainTypeElement, boolean superClassMethod, ConfigurationTypeElement configurationTypeElement, List<String> generated) {
		String fieldName = TransferObjectHelper.getFieldPath(method);

		if (contains(generated, fieldName)) {
			return null;
		}

		TypeElement domainElement = environmentContext.getProcessingEnv().getElementUtils().getTypeElement(domainTypeElement.getCanonicalName());

		ExecutableElement overrider = ProcessorUtils.getOverrider(domainElement, method, processingEnv);

		if (overrider != null) {
			method = overrider;
		}

		TransferObjectContext context = transferObjectContextProvider.get(configurationTypeElement, Modifier.PUBLIC, method, method, superClassMethod);
		if (context == null) {
			return null;
		}

		generated.add(fieldName);
		return context;
	}

	protected void printContexts(ConfigurationTypeElement configurationTypeElement, List<TransferObjectContext> contexts, List<String> generated, TransferObjectElementPrinter printer) {
		for (TransferObjectContext context: contexts) {
			printer.print(context);
		}
		
		printer.finish(configurationTypeElement);
	}

	private HistoryExecutableElementsList getMethodsForProcessings(ConfigurationTypeElement configurationTypeElement, MappingType mappingType, final DomainDeclaredType processingElement, DomainDeclaredType domainTypeElement) {

		HistoryExecutableElementsList result = new HistoryExecutableElementsList();

		List<ExecutableElement> methods = ElementFilter.methodsIn(processingElement.asConfigurationElement().getEnclosedElements());

		PojoElement pojoElement = new PojoElement(domainTypeElement.asConfigurationElement(), processingEnv);

		if (mappingType.equals(MappingType.AUTOMATIC)) {
			for (ExecutableElement method: methods) {

				boolean isProcessed = result.contains(method);
				boolean isGetter = MethodHelper.isGetterMethod(method);
				boolean hasSetter = pojoElement.hasSetterMethod(method);
				boolean isPublic = method.getModifiers().contains(Modifier.PUBLIC);

				if (!isProcessed && isGetter && hasSetter && isPublic) {
					result.add(method);
				} else if (!isProcessed && isGetter && !hasSetter && isPublic) {
					processingEnv.getMessager().printMessage(Kind.WARNING, "Method " + method.getSimpleName() + " does not have setter, type = " + processingElement.asConfigurationElement());
				}
			}
		}

		if (processingElement.getSuperClass() == null && processingElement.asConfigurationElement() != null) {
			TypeMirror superclass = processingElement.asConfigurationElement().getSuperclass();
			if (superclass.getKind().equals(TypeKind.DECLARED)) {
				result.addAll(getMethodsForProcessings(configurationTypeElement, mappingType, (DomainDeclaredType) processingEnv.getTransferObjectUtils().getDomainType(superclass), domainTypeElement));
			}
		} else if (processingElement.getSuperClass() != null) {

			List<ExecutableElement> superClassMethods = getMethodsForProcessings(configurationTypeElement, mappingType,
					processingElement.getSuperClass(), domainTypeElement);
			result.addAll(superClassMethods);

			List<ConfigurationTypeElement> configurationsForDomain = processingEnv.getEnvironmentContext().getConfigurationEnv().getConfigurationsForDomain(processingElement.getSuperClass());

			if (configurationsForDomain != null && configurationsForDomain.size() > 0) {
				//there is any configuration fot DTO
				//TODO but we have to choose first - choose that is not generated and does not delegate to other configuration
				//But still no correct solution because there might be more configuration for one domain
				// in order to produce multiple DTOs from one domain entity
				ConfigurationTypeElement superClassConfigutation = configurationsForDomain.get(0);

				MappingType superMappingType = getConfigurationMappingType(superClassConfigutation.asConfigurationElement());

				superClassMethods = getMethodsForProcessings(superClassConfigutation, superMappingType,
						processingElement.getSuperClass(), processingElement.getSuperClass());

				//TODO remove only those that are listed in explicit configuration - this assume, that configuation is AUTOMATIC
				result.removeAll(superClassMethods);
			}
		}

		//Do we need to iterate also interfaces?
//		for (TypeMirror domainInterface: processingElement.asConfigurationElement().getInterfaces()) {
//			getMethodsFroProcessings(configurationTypeElement, mappingType, (DomainDeclaredType) processingEnv.getTransferObjectUtils().getDomainType(domainInterface), domainTypeElement, generated, contexts);
//		}

		return result;
	}
}