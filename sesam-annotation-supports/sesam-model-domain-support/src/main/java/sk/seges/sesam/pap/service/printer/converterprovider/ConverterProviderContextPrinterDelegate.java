package sk.seges.sesam.pap.service.printer.converterprovider;

import sk.seges.sesam.core.pap.builder.api.ClassPathTypes;
import sk.seges.sesam.pap.converter.printer.AbstractProviderContextPrinterDelegate;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.provider.printer.AbstractProviderContextPrinter;

public class ConverterProviderContextPrinterDelegate extends AbstractProviderContextPrinterDelegate {

    public ConverterProviderContextPrinterDelegate(TransferObjectProcessingEnvironment processingEnv, ProviderConstructorParametersResolverProvider parametersResolverProvider, ClassPathTypes classPathTypes) {
        super(processingEnv, parametersResolverProvider, classPathTypes);
    }

    @Override
    protected AbstractProviderContextPrinter getProviderContextPrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        return new ConverterProviderContextPrinter(processingEnv, parametersResolverProvider);
    }

}