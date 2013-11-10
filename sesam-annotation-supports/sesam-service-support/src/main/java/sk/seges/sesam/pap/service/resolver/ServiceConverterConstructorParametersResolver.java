package sk.seges.sesam.pap.service.resolver;

import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableReferenceType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.resolver.DefaultConverterConstructorParametersResolver;

public class ServiceConverterConstructorParametersResolver extends DefaultConverterConstructorParametersResolver {

	public ServiceConverterConstructorParametersResolver(MutableProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	@Override
	protected PropagationType getConverterProviderContextParameterPropagation() {
		return PropagationType.PROPAGATED_IMUTABLE;
	}

	@Override
	protected PropagationType getConverterCacheParameterPropagation() {
		return PropagationType.PROPAGATED_MUTABLE;
	}
	
	@Override
	protected MutableReferenceType getConverterProviderContextReference() {
		return null;
	}
}