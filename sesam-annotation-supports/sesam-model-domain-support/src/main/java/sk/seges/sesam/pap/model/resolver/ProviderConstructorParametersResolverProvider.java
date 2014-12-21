package sk.seges.sesam.pap.model.resolver;

import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;

public interface ProviderConstructorParametersResolverProvider {

	public enum UsageType {
		DEFINITION,	//TODO rename - CONVERTER_PROVIDER_DEFINITION? or SERVICE_DEFINITION?
        PROVIDER_CONSTRUCTOR,
        PROVIDER_CONSTRUCTOR_USAGE,
        PROVIDER_INSIDE_USAGE,
        PROVIDER_OUTSIDE_USAGE,
        PROVIDER_CONTEXT_CONSTRUCTOR;
	}
	
	ConverterConstructorParametersResolver getParameterResolver(UsageType usageType);
}
