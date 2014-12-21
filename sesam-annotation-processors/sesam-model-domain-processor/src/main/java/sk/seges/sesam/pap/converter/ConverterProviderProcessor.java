package sk.seges.sesam.pap.converter;

import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.ConverterConstructorParameter;
import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.AbstractTransferProcessingProcessor;
import sk.seges.sesam.pap.converter.configurer.ConverterProviderProcessorConfigurer;
import sk.seges.sesam.pap.converter.model.ConverterProviderType;
import sk.seges.sesam.pap.converter.model.HasConstructorParameters;
import sk.seges.sesam.pap.converter.printer.api.ProviderElementPrinter;
import sk.seges.sesam.pap.converter.printer.base.ProviderTypePrinter;
import sk.seges.sesam.pap.converter.printer.converterprovider.DomainMethodConverterProviderPrinter;
import sk.seges.sesam.pap.converter.printer.converterprovider.DtoMethodConverterProviderPrinter;
import sk.seges.sesam.pap.converter.printer.model.ConverterProviderPrinterContext;
import sk.seges.sesam.pap.model.annotation.ConverterProviderDefinition;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.ConverterInstancerType;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;
import sk.seges.sesam.pap.model.provider.ClasspathConfigurationProvider;
import sk.seges.sesam.pap.model.provider.RoundEnvConfigurationProvider;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.model.resolver.CacheableConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.DefaultConverterConstructorParametersResolver;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider.UsageType;
import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.List;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ConverterProviderProcessor extends AbstractTransferProcessingProcessor {

	protected sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter converterProviderPrinter;

	@Override
	public ExecutionType getExecutionType() {
		return ExecutionType.ONCE;
	}
	
	@Override
	protected ProcessorConfigurer getConfigurer() {
		return new ConverterProviderProcessorConfigurer();
	}

	@Override
	protected MutableDeclaredType[] getOutputClasses(RoundContext context) {
		return new MutableDeclaredType[] {
			new ConverterProviderType(context.getMutableType(), processingEnv)
		};
	}

	@Override
	protected void printAnnotations(ProcessorContext context) {
		super.printAnnotations(context);
		context.getPrintWriter().println("@", ConverterProviderDefinition.class);
	}

	protected ProviderConstructorParametersResolverProvider getParametersResolverProvider() {
		return new CacheableConverterConstructorParametersResolverProvider() {

			@Override
			public ConverterConstructorParametersResolver constructParameterResolver(UsageType usageType) {
				switch (usageType) {
					case PROVIDER_INSIDE_USAGE:
						return new DefaultConverterConstructorParametersResolver(processingEnv) {
	
							@Override
							protected PropagationType getConverterCacheParameterPropagation() {
								return PropagationType.INSTANTIATED;
							}
						};

					case PROVIDER_OUTSIDE_USAGE:
						return new DefaultConverterConstructorParametersResolver(processingEnv);
					default:
						return new DefaultConverterConstructorParametersResolver(processingEnv);
				}
			}
		};
		
	}
	
	protected sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter getConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv) {
		return new sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter(processingEnv, getParametersResolverProvider(), UsageType.PROVIDER_INSIDE_USAGE) {
			@Override
			protected List<ConverterConstructorParameter> getConverterProviderMethodAdditionalParameters(ConverterTypeElement converterTypeElement, ConverterTargetType converterTargetType) {
				return new ArrayList<ConverterConstructorParameter>();
			}
		};
	}

	protected sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter ensureConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv) {
		if (converterProviderPrinter == null) {
			converterProviderPrinter = getConverterProviderPrinter(processingEnv);
		}
		
		return converterProviderPrinter;
	}

	protected ProviderElementPrinter[] getNestedPrinters(FormattedPrintWriter pw) {
		return new ProviderElementPrinter[] {
			new DomainMethodConverterProviderPrinter(processingEnv, pw, ensureConverterProviderPrinter(processingEnv)),
			new DtoMethodConverterProviderPrinter(processingEnv, pw, ensureConverterProviderPrinter(processingEnv))
		};
	}
	
	@Override
	protected void processElement(ProcessorContext context) {

        ProviderTypePrinter providerTypePrinter = new ProviderTypePrinter(getParametersResolverProvider());
		providerTypePrinter.initialize(environmentContext.getProcessingEnv(), (HasConstructorParameters) context.getOutputType(), UsageType.DEFINITION);

		for (ProviderElementPrinter nestedElementPrinter: getNestedPrinters(context.getPrintWriter())) {

			nestedElementPrinter.initialize();

			List<String> processedConfigurations = new ArrayList<String>();
			for (ConfigurationProvider configurationProvider: getLookupConfigurationProviders(context.getMutableType(), getEnvironmentContext(context.getMutableType()))) {
				List<ConfigurationTypeElement> availableConfigurations = configurationProvider.getAvailableConfigurations();
				
				for (ConfigurationTypeElement availableConfiguration: availableConfigurations) {
					if (!processedConfigurations.contains(availableConfiguration.getCanonicalName())) {
						
						List<ConfigurationTypeElement> configurationsForDomain = configurationProvider.getConfigurationsForDomain(availableConfiguration.getInstantiableDomain());

						for (ConfigurationTypeElement configurationForDomain: configurationsForDomain) {
							if (!processedConfigurations.contains(configurationForDomain.getCanonicalName())) {
								processedConfigurations.add(configurationForDomain.getCanonicalName());

								if (configurationForDomain.getConverter() != null) {
									ConverterProviderPrinterContext printerContext = new ConverterProviderPrinterContext(configurationForDomain.getDto(), configurationForDomain);
									nestedElementPrinter.print(printerContext);
								}
							}
						}
					}
				}
			}

			nestedElementPrinter.finish();

		}
		
		sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter converterProviderPrinter = ensureConverterProviderPrinter(processingEnv);
		UsageType previousUsage = converterProviderPrinter.changeUsage(UsageType.PROVIDER_OUTSIDE_USAGE);
		converterProviderPrinter.printConverterMethods(context.getOutputType(), false, ConverterInstancerType.REFERENCED_CONVERTER_INSTANCER);
		converterProviderPrinter.changeUsage(previousUsage);
		providerTypePrinter.finish((HasConstructorParameters) context.getOutputType());
	}

	@Override
	protected ConfigurationProvider[] getConfigurationProviders(MutableDeclaredType mutableType, EnvironmentContext<TransferObjectProcessingEnvironment> context) {
		return new ConfigurationProvider[] {
			new ClasspathConfigurationProvider(getClassPathTypes(), getEnvironmentContext(mutableType))
		};
	}

	protected ConfigurationProvider[] getLookupConfigurationProviders(MutableDeclaredType mutableType, EnvironmentContext<TransferObjectProcessingEnvironment> context) {
		return new ConfigurationProvider[] {
			new RoundEnvConfigurationProvider(getEnvironmentContext(mutableType))
		};
	}
}