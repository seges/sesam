package sk.seges.sesam.pap.provider;

import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.AbstractTransferProcessingProcessor;
import sk.seges.sesam.pap.converter.model.EntityProviderType;
import sk.seges.sesam.pap.converter.printer.api.ProviderElementPrinter;
import sk.seges.sesam.pap.converter.printer.entityprovider.DomainMethodEntityProviderPrinter;
import sk.seges.sesam.pap.converter.printer.model.EntityProviderPrinterContext;
import sk.seges.sesam.pap.model.annotation.EntityProviderDefinition;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.provider.ClasspathConfigurationProvider;
import sk.seges.sesam.pap.model.provider.RoundEnvConfigurationProvider;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.provider.configurer.EntityProviderProcessorConfigurer;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PeterSimun on 14.12.2014.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class EntityProviderProcessor extends AbstractTransferProcessingProcessor {

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.ONCE;
    }

    @Override
    protected ProcessorConfigurer getConfigurer() {
        return new EntityProviderProcessorConfigurer();
    }

    @Override
    protected MutableDeclaredType[] getOutputClasses(RoundContext context) {
        return new MutableDeclaredType[] {
                new EntityProviderType(context.getMutableType(), processingEnv)
        };
    }

    @Override
    protected void printAnnotations(ProcessorContext context) {
        super.printAnnotations(context);
        context.getPrintWriter().println("@", EntityProviderDefinition.class);
    }

    protected ProviderElementPrinter[] getNestedPrinters(FormattedPrintWriter pw) {
        return new ProviderElementPrinter[] {
                new DomainMethodEntityProviderPrinter(processingEnv, pw)
//                new DtoMethodConverterProviderPrinter(getParametersResolverProvider(), processingEnv, pw, ensureConverterProviderPrinter(processingEnv))
        };
    }

    @Override
    protected void processElement(ProcessorContext context) {

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
                                    EntityProviderPrinterContext printerContext = new EntityProviderPrinterContext(configurationForDomain.getDto(), configurationForDomain);
                                    nestedElementPrinter.print(printerContext);
                                }
                            }
                        }
                    }
                }
            }

            nestedElementPrinter.finish();

        }
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
