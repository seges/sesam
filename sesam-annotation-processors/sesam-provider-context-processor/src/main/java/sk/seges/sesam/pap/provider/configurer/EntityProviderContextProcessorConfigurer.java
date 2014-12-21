package sk.seges.sesam.pap.provider.configurer;

import sk.seges.sesam.core.pap.configuration.DelegateProcessorConfigurer;
import sk.seges.sesam.pap.model.annotation.EntityProviderContext;
import sk.seges.sesam.pap.model.annotation.EntityProviderContextDelegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class EntityProviderContextProcessorConfigurer extends DelegateProcessorConfigurer {

	@Override
	protected Type[] getConfigurationElement(DefaultConfigurationElement element) {
		switch (element) {
		case PROCESSING_ANNOTATIONS:
			return new Type[] { EntityProviderContext.class };
		}
		return new Type[] {};
	}

	@Override
	protected Class<? extends Annotation> getDelegatedAnnotationClass() {
		return EntityProviderContextDelegate.class;
	}

	@Override
	protected Annotation getAnnotationFromDelegate(Annotation annotationDelegate) {
		if (annotationDelegate instanceof EntityProviderContextDelegate) {
			return ((EntityProviderContextDelegate)annotationDelegate).value();
		}
		return null;
	}

}