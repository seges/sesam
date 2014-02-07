package sk.seges.sesam.pap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.api.Source;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableExecutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror.MutableTypeKind;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.printer.ConstantsPrinter;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.accessor.CopyAccessor;
import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.model.api.dto.DtoTypeVariable;
import sk.seges.sesam.pap.model.printer.accessors.AccessorsPrinter;
import sk.seges.sesam.pap.model.printer.api.TransferObjectElementPrinter;
import sk.seges.sesam.pap.model.printer.clone.ClonePrinter;
import sk.seges.sesam.pap.model.printer.constructor.*;
import sk.seges.sesam.pap.model.printer.equals.EqualsPrinter;
import sk.seges.sesam.pap.model.printer.field.FieldPrinter;
import sk.seges.sesam.pap.model.printer.hashcode.HashCodePrinter;
import sk.seges.sesam.pap.model.provider.ClasspathConfigurationProvider;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TransferObjectProcessor extends AbstractTransferProcessor {
	
	protected ConfigurationProvider[] getConfigurationProviders() {
		return new ConfigurationProvider[] {
				new ClasspathConfigurationProvider(getClassPathTypes(), getEnvironmentContext())
		};
	}
	
	@Override
	protected boolean checkPreconditions(ProcessorContext context, boolean alreadyExists) {
		//TODO context.getOutputClass is DTO, so use it!
		ConfigurationTypeElement configurationTypeElement = getConfigurationElement(context);
		if (!configurationTypeElement.getDto().isGenerated()) {
			return false;
		}
		return super.checkPreconditions(context, alreadyExists);
	}

	@Override
	protected MutableDeclaredType[] getOutputClasses(RoundContext context) {
		return new MutableDeclaredType[] {
				getConfigurationElement(context).getDto()
		};
	}
	
	@Override
	protected void processElement(ProcessorContext context) {
		new ConstantsPrinter(context.getPrintWriter(), processingEnv).copyConstants(context.getTypeElement());
		super.processElement(context);
	}
	
	@Override
	protected void printContexts(ConfigurationTypeElement configurationTypeElement, List<TransferObjectContext> contexts, List<String> generated, TransferObjectElementPrinter printer) {
		if (configurationTypeElement.asConfigurationElement() != null && configurationTypeElement.asConfigurationElement().asType().getKind().equals(TypeKind.DECLARED)) {
			TypeElement configurationType = (TypeElement) configurationTypeElement.asConfigurationElement();
			List<? extends TypeMirror> interfaces = configurationType.getInterfaces();
			
			for (TypeMirror interfaceType: interfaces) {
				if (interfaceType.getKind().equals(TypeKind.DECLARED)) {
					for (ExecutableElement method: ElementFilter.methodsIn(((DeclaredType)interfaceType).asElement().getEnclosedElements())) {
						
						if (MethodHelper.isGetterMethod(method)) {
							String fieldName = TransferObjectHelper.getFieldPath(method);
	
							if (!contains(generated, fieldName)) {
	
								TransferObjectContext context = transferObjectContextProvider.get(configurationTypeElement, Modifier.PUBLIC, method, null, false);
								if (context == null) {
									continue;
								}
								contexts.add(context);
							}
						}
					}
				}
			}
		}

		super.printContexts(configurationTypeElement, contexts, generated, printer);
	}

	protected String processMethodBody(String body) {
		Set<? extends Element> configurations = processingEnv.getEnvironmentContext().getRoundEnv().getElementsAnnotatedWith(TransferObjectMapping.class);

		for (Element configuration: configurations) {
			if (configuration.getAnnotation(Generated.class) == null && (configuration.getKind().isClass() || configuration.getKind().isInterface())) {

				ConfigurationContext configurationContext = new ConfigurationContext(processingEnv.getEnvironmentContext().getConfigurationEnv());
				ConfigurationTypeElement configurationTypeElement = new ConfigurationTypeElement(configuration, processingEnv.getEnvironmentContext(), configurationContext);
				configurationContext.addConfiguration(configurationTypeElement);

				String domainElementName = configurationTypeElement.getInstantiableDomainSpecified() == null ? null  :
						configurationTypeElement.getInstantiableDomainSpecified().getSimpleName().toString();

				if (domainElementName == null) {
					domainElementName = configurationTypeElement.getDomain().getSimpleName();
				}
				body = body.replaceAll("\\b" + domainElementName + "\\b", configurationTypeElement.getDto().getSimpleName());
			}
		}

		return body;
	}

	@Override
	protected void printAdditionalMethods(ProcessorContext context) {
		
		ConfigurationTypeElement configurationTypeElement = getConfigurationElement(context);

		List<ExecutableElement> overridenMethods = ElementFilter.methodsIn(context.getTypeElement().getEnclosedElements());

		for (ExecutableElement overridenMethod: overridenMethods) {
		
			CopyAccessor copyAccessor = new CopyAccessor(overridenMethod, processingEnv);
			
			if (copyAccessor.isMethodBodyCopied()) {
				TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(configurationTypeElement.getInstantiableDomain().getCanonicalName());
				//TODO remove typeElement when bug with type parameters is fixed
				Source elementSourceFile = getClassPathSources().getElementSourceFile(/*configurationTypeElement.getInstantiableDomain().asElement()*/typeElement);
				
				if (elementSourceFile == null) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "No source code available for class " + 
								configurationTypeElement.getInstantiableDomain().getCanonicalName() + ". Please add source class on the classpath also.");
				} else {
					ExecutableElement domainMethod = configurationTypeElement.getInstantiableDomain().getMethodByName(overridenMethod.getSimpleName().toString());

					if (domainMethod == null) {
						processingEnv.getMessager().printMessage(Kind.ERROR, "No method '" + overridenMethod.getSimpleName().toString() + "' is available in the class " +
									configurationTypeElement.getInstantiableDomain().getCanonicalName() + ". Please check your configuration " +
									configurationTypeElement.toString(ClassSerializer.SIMPLE) + ".");
						return;
					}

					String methodBody = elementSourceFile.getMethodBody(domainMethod);

					if (methodBody == null) {
						processingEnv.getMessager().printMessage(Kind.ERROR, "No method '" + overridenMethod.getSimpleName().toString() + "' is available in the class " +
								configurationTypeElement.getInstantiableDomain().getCanonicalName() + ". Please check your configuration " +
								configurationTypeElement.toString(ClassSerializer.SIMPLE) + ".");
						return;
					}

					if (methodBody.trim().startsWith("{")) {
						methodBody = methodBody.trim().substring(1);
					}

					if (methodBody.endsWith("}")) {
						methodBody = methodBody.substring(0, methodBody.length() - 1);
					}

					methodBody = processMethodBody(methodBody.trim());


					MutableExecutableType copiedMethod = processingEnv.getElementUtils().toMutableElement(overridenMethod).asType();

					List<? extends VariableElement> parameters = domainMethod.getParameters();
					List<MutableVariableElement> dtoParameters = new ArrayList<MutableVariableElement>();

					if (overridenMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
						copiedMethod.setReturnType(processingEnv.getTransferObjectUtils().getDomainType(domainMethod.getReturnType()).getDto());
					}

					int i = 0;
					int parametersCount = copiedMethod.getParameters().size();

					for (VariableElement methodParameter: parameters) {
						//TODO copy parameters annotations

						DtoType dto;

						if (i < parametersCount) {
							MutableVariableElement parameter = copiedMethod.getParameters().get(i);
							dto = processingEnv.getTransferObjectUtils().getDomainType(parameter.asType()).getDto();

							if (((MutableDeclaredType)parameter.asType()).hasTypeParameters()) {
								((DtoDeclaredType)dto).setTypeVariables(((MutableDeclaredType)parameter.asType()).getTypeVariables().toArray(new MutableTypeVariable[] {}));
							}

						} else {
							dto = processingEnv.getTransferObjectUtils().getDomainType(methodParameter.asType()).getDto();
						}

						i++;

						List<? extends MutableTypeVariable> typeVariables = null;
						
						if (dto.getKind().equals(MutableTypeKind.CLASS) || dto.getKind().equals(MutableTypeKind.INTERFACE)) {
							typeVariables = ((DtoDeclaredType)dto).getTypeVariables();
						}
						
						MutableVariableElement parameterElement = processingEnv.getElementUtils().getParameterElement(dto, methodParameter.getSimpleName().toString());
						
						if (typeVariables != null) {
							((MutableDeclaredType)parameterElement.asType()).setTypeVariables(typeVariables.toArray(new MutableTypeVariable[] {}));
						}
						
						dtoParameters.add(parameterElement);
					}
					
					copiedMethod.setParameters(dtoParameters);
					copiedMethod.setAnnotations((AnnotationMirror)null);
					copiedMethod.addModifier(Modifier.PUBLIC);
					
					context.getOutputType().addMethod(copiedMethod);
					
					copiedMethod.getPrintWriter().println(methodBody);
				}
			}
		}
	};

	@Override
	protected TransferObjectElementPrinter[] getElementPrinters(FormattedPrintWriter pw) {
		return new TransferObjectElementPrinter[] {
				new FieldPrinter(pw),
				new EmptyConstructorPrinter(pw),
				new EnumeratedConstructorDefinitionPrinter(processingEnv, pw),
				new EnumeratedConstructorBodyPrinter(processingEnv, pw),
				new ConstructorDefinitionPrinter(pw),
				new ConstructorBodyPrinter(pw),
				new AccessorsPrinter(processingEnv, pw),
				new EqualsPrinter(getEntityResolver(), processingEnv, pw),
				new HashCodePrinter(getEntityResolver(), processingEnv, pw),
				new ClonePrinter(processingEnv, pw),
		};
	}
}