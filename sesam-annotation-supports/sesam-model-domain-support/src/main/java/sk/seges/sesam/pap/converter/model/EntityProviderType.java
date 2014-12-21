package sk.seges.sesam.pap.converter.model;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.shared.model.provider.AbstractEntityMetaInfoProvider;

import javax.lang.model.element.Element;

public class EntityProviderType extends AbstractProviderType {

    public EntityProviderType(Element element, MutableProcessingEnvironment processingEnv) {
        super(element, processingEnv);
    }

    public EntityProviderType(MutableDeclaredType mutableType, MutableProcessingEnvironment processingEnv) {
        super(mutableType, processingEnv);
    }

    @Override
    protected Class<?> getProviderSuperClass() {
        return AbstractEntityMetaInfoProvider.class;
    }

    @Override
    protected String getProviderSuffix() {
        return "EntityProvider";
    }
}