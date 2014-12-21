package sk.seges.sesam.pap.converter.printer;

import sk.seges.sesam.core.pap.builder.api.ClassPathTypes;
import sk.seges.sesam.pap.converter.model.AbstractProviderContextType;
import sk.seges.sesam.pap.converter.model.AbstractProviderType;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.provider.printer.AbstractProviderContextPrinter;
import sk.seges.sesam.pap.service.printer.api.ProviderContextElementPrinter;

/**
 * Created by PeterSimun on 20.12.2014.
 */
public abstract class AbstractProviderContextPrinterDelegate implements ProviderContextElementPrinter {

    private final AbstractProviderContextPrinter providerContextPrinter;

    protected final ClassPathTypes classPathTypes;
    protected TransferObjectProcessingEnvironment processingEnv;

    public AbstractProviderContextPrinterDelegate(TransferObjectProcessingEnvironment processingEnv, ProviderConstructorParametersResolverProvider parametersResolverProvider,
                                                  ClassPathTypes classPathTypes) {
        this.processingEnv = processingEnv;
        this.providerContextPrinter = getProviderContextPrinter(parametersResolverProvider);
        this.classPathTypes = classPathTypes;
    }

    protected abstract AbstractProviderContextPrinter getProviderContextPrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider);

    @Override
    public void initialize(AbstractProviderContextType contextType) {
        providerContextPrinter.initialize(processingEnv, contextType,ProviderConstructorParametersResolverProvider.UsageType.PROVIDER_CONTEXT_CONSTRUCTOR);
    }

    @Override
    public void print(AbstractProviderType providerType) {
        providerContextPrinter.print(providerType.asElement());
    }

    @Override
    public void finish(AbstractProviderContextType contextType) {
        providerContextPrinter.finalize(contextType);
    }
}