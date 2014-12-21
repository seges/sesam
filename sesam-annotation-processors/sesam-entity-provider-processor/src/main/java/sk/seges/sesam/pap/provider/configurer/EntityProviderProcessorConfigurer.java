package sk.seges.sesam.pap.provider.configurer;

import sk.seges.sesam.core.pap.configuration.DefaultProcessorConfigurer;
import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;

import java.lang.reflect.Type;

/**
 * Created by PeterSimun on 14.12.2014.
 */
public class EntityProviderProcessorConfigurer extends DefaultProcessorConfigurer {

    @Override
    protected Type[] getConfigurationElement(DefaultConfigurationElement element) {
        switch (element) {
            case PROCESSING_ANNOTATIONS:
                return new Type[] { TransferObjectMapping.class };
        }
        return new Type[] {};
    }

}
