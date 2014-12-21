package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.pap.converter.printer.providercontext.ProviderContextTypePrinter;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;

/**
 * Created by PeterSimun on 20.12.2014.
 */
public class ConverterProviderContextTypePrinter extends ProviderContextTypePrinter {

    public ConverterProviderContextTypePrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        super(parametersResolverProvider);
    }

    protected Class<?> getResultClass() {
        return ConverterProviderContext.class;
    }

}
