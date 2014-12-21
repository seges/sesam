package sk.seges.sesam.pap.entityprovider.printer;

import sk.seges.sesam.core.pap.builder.api.ClassPathTypes;
import sk.seges.sesam.pap.converter.printer.AbstractProviderContextPrinterDelegate;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.provider.printer.AbstractProviderContextPrinter;

/**
 * Created by PeterSimun on 20.12.2014.
 */
public class EntityProviderContextPrinterDelegate extends AbstractProviderContextPrinterDelegate {

    public EntityProviderContextPrinterDelegate(TransferObjectProcessingEnvironment processingEnv, ProviderConstructorParametersResolverProvider parametersResolverProvider, ClassPathTypes classPathTypes) {
        super(processingEnv, parametersResolverProvider, classPathTypes);
    }

    @Override
    protected AbstractProviderContextPrinter getProviderContextPrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        return new EntityProviderContextPrinter(processingEnv, parametersResolverProvider);
    }
}
