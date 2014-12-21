package sk.seges.sesam.pap.entityprovider.printer;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.converter.printer.base.ProviderTypePrinter;
import sk.seges.sesam.pap.converter.printer.entityprovider.EntityProviderContextTypePrinter;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.provider.printer.AbstractProviderContextPrinter;

/**
 * Created by PeterSimun on 20.12.2014.
 */
public class EntityProviderContextPrinter extends AbstractProviderContextPrinter {

    public EntityProviderContextPrinter(MutableProcessingEnvironment processingEnv, ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        super(processingEnv, parametersResolverProvider);
    }

    @Override
    protected String getRegisterMethodName() {
        return "registerEntityProvider";
    }

    @Override
    protected ProviderTypePrinter getProviderTypePrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        return new EntityProviderContextTypePrinter(parametersResolverProvider);
    }
}
