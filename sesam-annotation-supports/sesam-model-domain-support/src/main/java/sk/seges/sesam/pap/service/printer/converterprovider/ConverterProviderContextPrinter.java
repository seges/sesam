package sk.seges.sesam.pap.service.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.converter.printer.base.ProviderTypePrinter;
import sk.seges.sesam.pap.converter.printer.converterprovider.ConverterProviderContextTypePrinter;
import sk.seges.sesam.pap.converter.printer.providercontext.ProviderContextTypePrinter;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.provider.printer.AbstractProviderContextPrinter;

public class ConverterProviderContextPrinter extends AbstractProviderContextPrinter {

    public ConverterProviderContextPrinter(MutableProcessingEnvironment processingEnv, ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        super(processingEnv, parametersResolverProvider);
    }

    @Override
    protected String getRegisterMethodName() {
        return "registerConverterProvider";
    }

    @Override
    protected ProviderTypePrinter getProviderTypePrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        return new ConverterProviderContextTypePrinter(parametersResolverProvider);
    }
}