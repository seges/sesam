package sk.seges.sesam.pap.converter.printer.entityprovider;

import sk.seges.sesam.pap.converter.printer.providercontext.ProviderContextTypePrinter;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;
import sk.seges.sesam.shared.model.converter.EntityProviderContext;

/**
 * Created by PeterSimun on 20.12.2014.
 */
public class EntityProviderContextTypePrinter extends ProviderContextTypePrinter {

    public EntityProviderContextTypePrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        super(parametersResolverProvider);
    }

    protected Class<?> getResultClass() {
        return EntityProviderContext.class;
    }

}
