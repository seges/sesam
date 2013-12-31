package sk.seges.sesam.pap.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import sk.seges.sesam.core.pap.comparator.ExecutableComparator;
import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType.RenameActionType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror.MutableTypeKind;
import sk.seges.sesam.core.pap.processor.MutableAnnotationProcessor;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.model.ConfigurationEnvironment;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.DomainDeclared;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.provider.ConfigurationCache;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.service.annotation.LocalServiceDefinition;
import sk.seges.sesam.pap.service.annotation.RemoteServiceDefinition;
import sk.seges.sesam.pap.service.configurer.ServiceInterfaceProcessorConfigurer;
import sk.seges.sesam.pap.service.model.RemoteServiceTypeElement;
import sk.seges.sesam.pap.service.provider.RemoteServiceCollectorConfigurationProvider;
import sk.seges.sesam.pap.service.utils.TypeUtils;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ServiceInterfaceProcessor extends MutableAnnotationProcessor {

	protected TransferObjectProcessingEnvironment processingEnv;
	protected EnvironmentContext<TransferObjectProcessingEnvironment> environmentContext;

	@Override
	protected ProcessorConfigurer getConfigurer() {
		return new ServiceInterfaceProcessorConfigurer();
	}

	@Override
	protected MutableDeclaredType[] getOutputClasses(RoundContext context) {
		return new MutableDeclaredType[] {
				new RemoteServiceTypeElement(context.getTypeElement(), processingEnv).getLocalServiceElement()				
		};
	}
	
	@Override
	protected void printAnnotations(ProcessorContext context) {
		context.getPrintWriter().println("@", LocalServiceDefinition.class, "(remoteService = " + context.getTypeElement().toString() + ".class)");
	}
	
	protected ConfigurationProvider[] getConfigurationProviders(RemoteServiceTypeElement remoteServiceInterfaceElement, EnvironmentContext<TransferObjectProcessingEnvironment> context) {
		return new ConfigurationProvider[] {
				new RemoteServiceCollectorConfigurationProvider(remoteServiceInterfaceElement, getClassPathTypes(), context)
		};
	}

	protected ConfigurationCache getConfigurationCache() {
		return new ConfigurationCache();
	}

	protected EnvironmentContext<TransferObjectProcessingEnvironment> getEnvironmentContext(RemoteServiceTypeElement remoteServiceInterfaceElement) {
		if (environmentContext == null) {
			ConfigurationEnvironment configurationEnv = new ConfigurationEnvironment(processingEnv, roundEnv, getConfigurationCache());
			environmentContext = configurationEnv.getEnvironmentContext();
			configurationEnv.setConfigurationProviders(getConfigurationProviders(remoteServiceInterfaceElement, environmentContext));
		}
		
		return environmentContext;
	}

	@Override
	protected void init(Element element, RoundEnvironment roundEnv) {
		super.init(element, roundEnv);
		RemoteServiceTypeElement remoteServiceTypeElement = new RemoteServiceTypeElement((TypeElement)element, super.processingEnv);
		this.processingEnv = new TransferObjectProcessingEnvironment(getProcessingEnv(), roundEnv, getConfigurationCache(), getClass(), getProcessingEnv().getUsedTypes());
		EnvironmentContext<TransferObjectProcessingEnvironment> context = getEnvironmentContext(remoteServiceTypeElement);
		this.processingEnv.setConfigurationProviders(getConfigurationProviders(remoteServiceTypeElement, context));
	}

	@Override
	protected void processElement(ProcessorContext context) {

		for (TypeMirror interfaceType: context.getTypeElement().getInterfaces()) {
			if (interfaceType.getKind().equals(TypeKind.DECLARED)) {
				Element interfaceElement = ((DeclaredType) interfaceType).asElement();
				if (isRemoteService(interfaceElement)) {
					context.getOutputType().addInterface(new RemoteServiceTypeElement((TypeElement)interfaceElement, processingEnv).getLocalServiceElement());
				}
			}
		}

		processMethods(context.getTypeElement(), context.getPrintWriter(), false);
	}

	protected void processMethods(TypeElement element, FormattedPrintWriter pw, boolean nested) {
		RemoteServiceTypeElement remoteServiceTypeElement = new RemoteServiceTypeElement(element, processingEnv);

		List<ExecutableElement> methods = ElementFilter.methodsIn(element.getEnclosedElements());

		Collections.sort(methods, new ExecutableComparator());

		for (ExecutableElement method: methods) {

			List<MutableTypeMirror> params = new LinkedList<MutableTypeMirror>();
			List<MutableTypeMirror> types = new LinkedList<MutableTypeMirror>();

			for (VariableElement parameter: method.getParameters()) {
				DtoType dtoParamType = processingEnv.getTransferObjectUtils().getDtoType(parameter.asType());
				DomainType domain = dtoParamType.getDomain();
				params.add(domain);
				if (!TypeUtils.containsSameType(types, domain)) {
					types.add(domain);
				}
			}

			DtoType dtoReturnType = processingEnv.getTransferObjectUtils().getDtoType(method.getReturnType());
			DomainType domainReturnType = dtoReturnType.getDomain();

			if (!TypeUtils.containsSameType(types, domainReturnType)) {
				types.add(domainReturnType);
			}

			for (TypeParameterElement typeParameter: method.getTypeParameters()) {

				DomainType domainParameter = processingEnv.getTransferObjectUtils().getDtoType(typeParameter.asType()).getDomain();

				if (!TypeUtils.containsSameType(types, domainParameter)) {
					types.add(domainParameter);
				}
			}

			remoteServiceTypeElement.getLocalServiceElement().printMethodTypeVariablesDefinition(types, pw);

			if (domainReturnType.getKind().equals(MutableTypeKind.CLASS) || domainReturnType.getKind().equals(MutableTypeKind.INTERFACE)) {
				pw.print(prefixDomainParameter(((MutableDeclaredType) remoteServiceTypeElement.toReturnType((DomainDeclared)domainReturnType))).clone());
			} else {
				pw.print(remoteServiceTypeElement.toReturnType(domainReturnType));
			}

			pw.print(" " + method.getSimpleName().toString() + "(");

			int i = 0;
			for (VariableElement parameter: method.getParameters()) {
				if (i > 0) {
					pw.print(", ");
				}

				DomainType domain = processingEnv.getTransferObjectUtils().getDtoType(remoteServiceTypeElement.toParamType(parameter.asType())).getDomain();

				if (domain.getKind().equals(MutableTypeKind.CLASS) || domain.getKind().equals(MutableTypeKind.INTERFACE)) {
					pw.print(prefixDomainParameter(((MutableDeclaredType)domain).clone()).stripTypeParametersTypes());
				}  else {
					pw.print(domain);
				}
				pw.print(" " + parameter.getSimpleName().toString());
				i++;
			}

			pw.print(")");

			if (method.getThrownTypes().size() > 0) {
				pw.print(" throws ");
				int j = 0;
				for (TypeMirror thrownType: method.getThrownTypes()) {
					if (j > 0) {
						pw.print(", ");
					}
					pw.print(thrownType);
					j++;
				}
			}
			pw.println(";");
			pw.println();
		}

		for (TypeMirror interfaceType: element.getInterfaces()) {
			if (interfaceType.getKind().equals(TypeKind.DECLARED)) {
				Element interfaceElement = ((DeclaredType) interfaceType).asElement();
				if (nested || !isRemoteService(interfaceElement)) {
					processMethods((TypeElement)interfaceElement, pw, true);
				}
			}
		}

	}

	protected boolean isRemoteService(Element element) {
		if (!element.getKind().equals(ElementKind.INTERFACE)) {
			//it is not an interface, remote service should be an interface
			return false;
		}

		return element.getAnnotation(RemoteServiceDefinition.class) != null;
	}

	private MutableDeclaredType prefixDomainParameter(MutableDeclaredType type) {
		type.renameTypeParameter(RenameActionType.PREFIX, ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX + "_", null, true);
		return type;
	}
}