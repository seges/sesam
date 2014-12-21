package sk.seges.sesam.pap.provider;

import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.pap.AbstractTransferProcessingProcessor;
import sk.seges.sesam.pap.converter.model.EntityProviderType;
import sk.seges.sesam.pap.entityprovider.printer.EntityProviderContextPrinterDelegate;
import sk.seges.sesam.pap.model.annotation.EntityProviderDefinition;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.provider.ClasspathConfigurationProvider;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.model.resolver.CacheableConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;
import sk.seges.sesam.pap.provider.configurer.EntityProviderContextProcessorConfigurer;
import sk.seges.sesam.pap.service.model.EntityProviderContextType;
import sk.seges.sesam.pap.service.printer.api.ProviderContextElementPrinter;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EntityProviderContextProcessor extends AbstractTransferProcessingProcessor {

    @Override
	protected ProcessorConfigurer getConfigurer() {
		return new EntityProviderContextProcessorConfigurer();
	}

    @Override
	protected MutableDeclaredType[] getOutputClasses(RoundContext context) {
		return new MutableDeclaredType[] {
				new EntityProviderContextType(context.getMutableType(), processingEnv)
		};
	}

    @Override
    protected void processElement(ProcessorContext context) {

        EntityProviderContextType contextType = (EntityProviderContextType)context.getOutputType();

        for (ProviderContextElementPrinter elementPrinter: getElementPrinters(contextType)) {
            elementPrinter.initialize(contextType);

            Set<? extends Element> entityProviders = getClassPathTypes().getElementsAnnotatedWith(EntityProviderDefinition.class);

            for (Element entityProvider: entityProviders) {
                elementPrinter.print(new EntityProviderType(entityProvider, processingEnv));
            }

            elementPrinter.finish(contextType);
        }
    }

    protected ProviderContextElementPrinter[] getElementPrinters(MutableDeclaredType contextType) {
		return new ProviderContextElementPrinter[] {
				new EntityProviderContextPrinterDelegate(processingEnv, getParametersResolverProvider(contextType), getClassPathTypes())
		};
	}

    protected ProviderConstructorParametersResolverProvider getParametersResolverProvider(MutableDeclaredType contextType) {
        return new CacheableConverterConstructorParametersResolverProvider() {
            @Override
            public ConverterConstructorParametersResolver constructParameterResolver(UsageType usageType) {
                return new ConverterConstructorParametersResolver() {
                    @Override
                    public ParameterElement[] getConstructorAditionalParameters() {
                        return new ParameterElement[0];
                    }
                };
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