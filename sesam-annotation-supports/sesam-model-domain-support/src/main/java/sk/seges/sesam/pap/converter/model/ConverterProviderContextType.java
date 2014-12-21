package sk.seges.sesam.pap.converter.model;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.converter.model.AbstractProviderContextType;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;

public class ConverterProviderContextType extends AbstractProviderContextType {

	public static final String CONVERTER_PROVIDER_CONTEXT_SUFFIX = "ConverterProviderContext";

    public ConverterProviderContextType(MutableDeclaredType type, MutableProcessingEnvironment processingEnv) {
        super(type, processingEnv);
    }

    protected Class<?> getContextSuperClass() {
        return ConverterProviderContext.class;
    }

    @Override
    protected String getSufix() {
        return CONVERTER_PROVIDER_CONTEXT_SUFFIX;
    }
}