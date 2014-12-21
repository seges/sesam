package sk.seges.sesam.pap.converter.printer.base;

import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.pap.converter.model.HasConstructorParameters;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;

public class ProviderTypePrinter {

    protected final ProviderConstructorParametersResolverProvider parametersResolverProvider;

	public ProviderTypePrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
		this.parametersResolverProvider = parametersResolverProvider;
	}

    public void initialize(MutableProcessingEnvironment processingEnv, final HasConstructorParameters type, ProviderConstructorParametersResolverProvider.UsageType usageType) {

        ParameterElement[] generatedParameters = type.getConverterParameters(parametersResolverProvider.getParameterResolver(usageType));

        for (ParameterElement generatedParameter : generatedParameters) {
            ProcessorUtils.addField(processingEnv, type, generatedParameter.getType(), generatedParameter.getName());
        }
    }

    public void finish(final HasConstructorParameters type) {
    }
}