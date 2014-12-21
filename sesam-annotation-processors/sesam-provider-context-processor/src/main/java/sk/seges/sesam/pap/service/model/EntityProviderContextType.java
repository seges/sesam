package sk.seges.sesam.pap.service.model;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.converter.model.AbstractProviderContextType;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;
import sk.seges.sesam.shared.model.converter.EntityProviderContext;

public class EntityProviderContextType extends AbstractProviderContextType {

	public static final String ENTITY_PROVIDER_CONTEXT_SUFFIX = "EntityProviderContext";

    public EntityProviderContextType(MutableDeclaredType type, MutableProcessingEnvironment processingEnv) {
        super(type, processingEnv);
    }

    protected Class<?> getContextSuperClass() {
        return EntityProviderContext.class;
    }

    @Override
    protected String getSufix() {
        return ENTITY_PROVIDER_CONTEXT_SUFFIX;
    }
}