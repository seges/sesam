package sk.seges.sesam.pap.service;

import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.pap.AbstractTransferProcessingProcessor;
import sk.seges.sesam.pap.converter.model.ConverterProviderContextType;
import sk.seges.sesam.pap.converter.model.ConverterProviderType;
import sk.seges.sesam.pap.model.annotation.ConverterProviderDefinition;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.provider.ClasspathConfigurationProvider;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.model.resolver.CacheableConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.DefaultConverterConstructorParametersResolver;
import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;
import sk.seges.sesam.pap.service.configurer.ServiceConverterContextProcessorConfigurer;
import sk.seges.sesam.pap.service.printer.api.ProviderContextElementPrinter;
import sk.seges.sesam.pap.service.printer.converterprovider.ConverterProviderContextPrinterDelegate;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ServiceConverterContextProcessor extends AbstractTransferProcessingProcessor {

    @Override
	protected ProcessorConfigurer getConfigurer() {
		return new ServiceConverterContextProcessorConfigurer();
	}

    @Override
	protected MutableDeclaredType[] getOutputClasses(RoundContext context) {
		return new MutableDeclaredType[] {
				new ConverterProviderContextType(context.getMutableType(), processingEnv)
		};
	}

    @Override
    protected void processElement(ProcessorContext context) {

        ConverterProviderContextType contextType = (ConverterProviderContextType)context.getOutputType();

        for (ProviderContextElementPrinter elementPrinter: getElementPrinters(contextType)) {
            elementPrinter.initialize(contextType);

            Set<? extends Element> converterProviders = getClassPathTypes().getElementsAnnotatedWith(ConverterProviderDefinition.class);

            for (Element converterProvider: converterProviders) {
                elementPrinter.print(new ConverterProviderType(converterProvider, processingEnv));
            }

            elementPrinter.finish(contextType);
        }
    }

    protected ProviderContextElementPrinter[] getElementPrinters(MutableDeclaredType contextType) {
		return new ProviderContextElementPrinter[] {
				new ConverterProviderContextPrinterDelegate(processingEnv, getParametersResolverProvider(contextType), getClassPathTypes())
		};
	}

    protected ProviderConstructorParametersResolverProvider getParametersResolverProvider(MutableDeclaredType contextType) {
        return new CacheableConverterConstructorParametersResolverProvider() {
            @Override
            public ConverterConstructorParametersResolver constructParameterResolver(UsageType usageType) {
                return new DefaultConverterConstructorParametersResolver(processingEnv);
            }
        };
    }

    @Override
    protected ConfigurationProvider[] getConfigurationProviders(MutableDeclaredType service, EnvironmentContext<TransferObjectProcessingEnvironment> context) {
        return new ConfigurationProvider[] {
                new ClasspathConfigurationProvider(getClassPathTypes(), context)
        };
    }
}