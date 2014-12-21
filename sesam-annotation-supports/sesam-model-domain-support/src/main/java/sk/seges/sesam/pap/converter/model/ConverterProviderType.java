package sk.seges.sesam.pap.converter.model;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.shared.model.converter.provider.AbstractConverterProvider;

import javax.lang.model.element.Element;

public class ConverterProviderType extends AbstractProviderType {

	public static final String CONVERTER_PROVIDER_SUFFIX = "ConverterProvider";

    public ConverterProviderType(Element element, MutableProcessingEnvironment processingEnv) {
        super(element, processingEnv);
    }

    public ConverterProviderType(MutableDeclaredType mutableType, MutableProcessingEnvironment processingEnv) {
        super(mutableType, processingEnv);
    }

    protected Class<?> getProviderSuperClass() {
        return AbstractConverterProvider.class;
    }

    @Override
    protected String getProviderSuffix() {
        return CONVERTER_PROVIDER_SUFFIX;
    }
}