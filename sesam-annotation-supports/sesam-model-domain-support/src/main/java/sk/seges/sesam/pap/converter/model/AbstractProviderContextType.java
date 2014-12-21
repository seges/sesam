package sk.seges.sesam.pap.converter.model;

import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.delegate.DelegateMutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.converter.model.HasConstructorParameters;
import sk.seges.sesam.pap.converter.util.HasConstructorParametersDelegate;
import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;

import javax.lang.model.element.Modifier;

public abstract class AbstractProviderContextType extends DelegateMutableDeclaredType implements HasConstructorParameters {

    protected final MutableDeclaredType type;
    protected final MutableProcessingEnvironment processingEnv;

    protected AbstractProviderContextType(MutableDeclaredType type, MutableProcessingEnvironment processingEnv) {
        this.type = type;
        this.processingEnv = processingEnv;

        addModifier(Modifier.PUBLIC);
        setKind(MutableTypeKind.CLASS);
        setSuperClass(processingEnv.getTypeUtils().toMutableType(getContextSuperClass()));
    }

    protected abstract Class<?> getContextSuperClass();

    @Override
    protected MutableDeclaredType getDelegate() {
        return type.clone().setSimpleName(type.getSimpleName() + getSufix());
    }

    protected abstract String getSufix();

    @Override
    public ParameterElement[] getConverterParameters(ConverterConstructorParametersResolver parametersResolver) {
        return new HasConstructorParametersDelegate().getConverterParameters(processingEnv, parametersResolver);
    }
}