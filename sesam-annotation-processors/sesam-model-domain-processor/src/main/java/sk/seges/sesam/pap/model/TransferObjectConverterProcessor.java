package sk.seges.sesam.pap.model;

import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableExecutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.printer.ConstructorPrinter;
import sk.seges.sesam.core.pap.structure.DefaultPackageValidatorProvider;
import sk.seges.sesam.core.pap.structure.api.PackageValidatorProvider;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectMappingAccessor;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.ElementHolderTypeConverter;
import sk.seges.sesam.pap.model.printer.api.TransferObjectElementPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterInstancerType;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.equals.ConverterEqualsPrinter;
import sk.seges.sesam.pap.model.printer.method.CopyFromDtoPrinter;
import sk.seges.sesam.pap.model.printer.method.CopyToDtoPrinter;
import sk.seges.sesam.pap.model.provider.ClasspathConfigurationProvider;
import sk.seges.sesam.pap.model.provider.TransferObjectConverterProcessorContextProvider;
import sk.seges.sesam.pap.model.provider.TransferObjectProcessorContextProvider;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.model.resolver.CacheableConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider.UsageType;
import sk.seges.sesam.pap.model.resolver.DefaultConverterConstructorParametersResolver;
import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;
import sk.seges.sesam.shared.model.converter.BasicCachedConverter;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TransferObjectConverterProcessor extends AbstractTransferProcessor {

	protected ConverterProviderPrinter converterProviderPrinter;
	protected Set<String> nestedInstances = new HashSet<String>();

	protected ElementHolderTypeConverter getElementTypeConverter() {
		return new ElementHolderTypeConverter() {

			@Override
			public TypeMirror getIterableDtoType(MutableTypeMirror collectionType) {
				return null;
			}
		};
	}
	
	protected ConfigurationProvider[] getConfigurationProviders() {
		return new ConfigurationProvider[] {
				new ClasspathConfigurationProvider(getClassPathTypes(), getEnvironmentContext())
		};
	}

	@Override
	protected boolean checkPreconditions(ProcessorContext context, boolean alreadyExists) {

		if (!ConverterProcessingHelper.isConverterGenerated(context.getTypeElement(), processingEnv)) {
			return false;
		}

		ConfigurationTypeElement configurationElement = getConfigurationElement(context);

		ConverterTypeElement converter = configurationElement.getConverter();
		if (converter == null || !converter.isGenerated()) {
			return false;
		}

		return super.checkPreconditions(context, alreadyExists);
	}
	
	@Override
	protected MutableDeclaredType[] getOutputClasses(RoundContext context) {

		TypeElement domain = new TransferObjectMappingAccessor(context.getTypeElement(), processingEnv).getDomain();

		if (domain == null) {
			//if there no domain defined, converter does not make sense
			return new MutableDeclaredType[] {};
		}

		if (domain.getModifiers().contains(Modifier.ABSTRACT)) {
			return new MutableDeclaredType[] {};
		}

		ConverterTypeElement converter = getConfigurationElement(context).getConverter();
		if (converter == null || !converter.isGenerated()) {
			return new MutableDeclaredType[] {};
		}

		return new MutableDeclaredType[] { converter };
	}
	
	protected PackageValidatorProvider getPackageValidatorProvider() {
		return new DefaultPackageValidatorProvider();
	}

	@Override
	protected TransferObjectElementPrinter[] getElementPrinters(FormattedPrintWriter pw) {
		return new TransferObjectElementPrinter[] {
				new ConverterEqualsPrinter(converterProviderPrinter, getEntityResolver(), getParametersResolverProvider(), processingEnv, pw),
				new CopyToDtoPrinter(converterProviderPrinter, getElementTypeConverter(),getEntityResolver(), getParametersResolverProvider(), roundEnv, processingEnv, pw),
				new CopyFromDtoPrinter(nestedInstances, converterProviderPrinter, getEntityResolver(), getParametersResolverProvider(), roundEnv, processingEnv, pw)
		};
	}
	
	protected TransferObjectProcessorContextProvider getProcessorContextProvider(TransferObjectProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
		return new TransferObjectConverterProcessorContextProvider(getEnvironmentContext(), getEntityResolver());
	}

	protected ConverterConstructorParametersResolverProvider getParametersResolverProvider() {
		return new CacheableConverterConstructorParametersResolverProvider() {
			
			@Override
			public ConverterConstructorParametersResolver constructParameterResolver(UsageType usageType) {
				return new DefaultConverterConstructorParametersResolver(processingEnv);
			}
		};
	}
	
	protected ConverterProviderPrinter getConverterProviderPrinter(FormattedPrintWriter pw) {
		return new ConverterProviderPrinter(processingEnv, getParametersResolverProvider(), UsageType.CONVERTER_PROVIDER_OUTSIDE_USAGE);
	}
	
	@Override
	protected void processElement(ProcessorContext context) {

		FormattedPrintWriter pw = context.getPrintWriter();
		
		converterProviderPrinter = getConverterProviderPrinter(pw);

		TypeElement cachedConverterType = processingEnv.getElementUtils().getTypeElement(BasicCachedConverter.class.getCanonicalName());
		
		ParameterElement[] constructorAdditionalParameters = getParametersResolverProvider().getParameterResolver(UsageType.DEFINITION).getConstructorAditionalParameters();
		
		ConstructorPrinter constructorPrinter = new ConstructorPrinter(context.getOutputType(), processingEnv);
		constructorPrinter.printConstructors(cachedConverterType, constructorAdditionalParameters);

		TypeElement superClassElement = context.getOutputType().getSuperClass() != null ? context.getOutputType().getSuperClass().asElement() : null;

		for (ParameterElement constructorAdditionalParameter: constructorAdditionalParameters) {
			String parameterName = constructorAdditionalParameter.getName().toString();
			if (constructorAdditionalParameter.getPropagationType().equals(PropagationType.PROPAGATED_MUTABLE) && !containsField(superClassElement, constructorAdditionalParameter)) {
				MutableVariableElement field = processingEnv.getElementUtils().getParameterElement(constructorAdditionalParameter.getType(), parameterName);
				context.getOutputType().addField((MutableVariableElement) field.addModifier(Modifier.PROTECTED));

				MutableExecutableType setterMethod = processingEnv.getTypeUtils().getExecutable(
						processingEnv.getTypeUtils().toMutableType(processingEnv.getTypeUtils().getNoType(TypeKind.VOID)), MethodHelper.toSetter(parameterName)).
						addParameter(processingEnv.getElementUtils().getParameterElement(constructorAdditionalParameter.getType(), parameterName)).addModifier(Modifier.PUBLIC);
				context.getOutputType().addMethod(setterMethod);
				setterMethod.getPrintWriter().println("this." + parameterName + " = " + parameterName + ";");
			}
		}

		super.processElement(context);
		
		converterProviderPrinter.printConverterMethods(context.getOutputType(), false, ConverterInstancerType.REFERENCED_CONVERTER_INSTANCER);
	}

	private boolean containsField(TypeElement superClassElement, ParameterElement constructorAdditionalParameter) {

		if (superClassElement == null) {
			return false;
		}

		List<VariableElement> fields = ElementFilter.fieldsIn(superClassElement.getEnclosedElements());

		for (VariableElement field: fields) {
			if (constructorAdditionalParameter.getType().isSameType(processingEnv.getTypeUtils().toMutableType(field.asType()))) {
				return true;
			}
		}

		if (superClassElement.getSuperclass().getKind().equals(TypeKind.DECLARED)) {
			return containsField((TypeElement)((DeclaredType)superClassElement.getSuperclass()).asElement(), constructorAdditionalParameter);
		}

		return false;
	}
}