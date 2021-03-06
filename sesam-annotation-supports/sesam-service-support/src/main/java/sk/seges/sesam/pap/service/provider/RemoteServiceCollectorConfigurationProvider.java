package sk.seges.sesam.pap.service.provider;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.DeclaredType;

import sk.seges.sesam.core.pap.builder.api.ClassPathTypes;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.service.model.RemoteServiceTypeElement;

public class RemoteServiceCollectorConfigurationProvider extends AbstractServiceCollectorConfigurationProvider {

	protected final RemoteServiceTypeElement remoteService;
	private List<ConfigurationTypeElement> configurations = null;

	public RemoteServiceCollectorConfigurationProvider(RemoteServiceTypeElement remoteService, ClassPathTypes classpathUtils, EnvironmentContext<TransferObjectProcessingEnvironment> envContext) {
		super(classpathUtils, envContext);
		this.remoteService = remoteService;
	}

	protected List<ConfigurationTypeElement> collectConfigurations() {

		if (configurations != null) {
			return configurations;
		}
		
		ArrayList<String> processedElements = new ArrayList<String>();

		List<ConfigurationTypeElement> result = new ArrayList<ConfigurationTypeElement>();
		result.addAll(getConfigurationsFromType((DeclaredType)remoteService.asElement().asType(), processedElements));
		
		this.configurations = result;

		return result;
	}
}
