package sk.seges.sesam.shared.model.converter;

import sk.seges.sesam.shared.model.provider.api.EntityMetaInfoProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityProviderContext {

	protected List<EntityMetaInfoProvider> availableEntityProviders = new ArrayList<EntityMetaInfoProvider>();

	public void registerEntityProvider(EntityMetaInfoProvider entityMetaInfoProvider) {
		availableEntityProviders.add(entityMetaInfoProvider);
	}

	public abstract EntityProviderContext get();

    public List<Class<?>> getDtoClassForDomain(String domainClassName) {
        if (domainClassName == null) {
            return null;
        }

        Class<?> domainClass;

        try {
            domainClass = Class.forName(domainClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }

        List<Class<?>> result = new ArrayList<Class<?>>();

        for (EntityMetaInfoProvider availableEntityProvider: availableEntityProviders) {
            List<Class<?>> dtoClassForDomain = availableEntityProvider.getDtoClassForDomain(domainClass);

            if (dtoClassForDomain != null) {
                result.addAll(dtoClassForDomain);
            }
        }

        return result;
    }
}