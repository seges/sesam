package sk.seges.sesam.pap.model.model;

import java.util.List;

import sk.seges.sesam.core.pap.model.InitializableValue;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;

public abstract class TomDeclaredConfigurationHolder extends TomBaseDeclaredType {

	protected ConfigurationContext configurationContext;
	
	protected TomDeclaredConfigurationHolder(EnvironmentContext<TransferObjectProcessingEnvironment> environmentContext, ConfigurationContext configurationContext) {
		super(environmentContext);
		this.configurationContext = configurationContext;
	}

	protected ConfigurationContext ensureConfigurationContext() {
		if (configurationContext == null) {
			configurationContext = new ConfigurationContext(environmentContext.getConfigurationEnv());
			configurationContext.setConfigurations(getConfigurationsForType());
		}
		
		return configurationContext;
	}
	
	public ConfigurationProvider[] getConfigurationProviders() {
		return environmentContext.getConfigurationEnv().getConfigurationProviders();
	};

	private InitializableValue<ConfigurationTypeElement> domainDefinitionConfigurationValue = new InitializableValue<ConfigurationTypeElement>();
	private InitializableValue<ConfigurationTypeElement> converterDefinitionConfigurationValue = new InitializableValue<ConfigurationTypeElement>();

	public ConfigurationTypeElement getDomainDefinitionConfiguration() {
		if (domainDefinitionConfigurationValue.isInitialized()) {
			return domainDefinitionConfigurationValue.getValue();
		}
		return domainDefinitionConfigurationValue.setValue(ensureConfigurationContext().getDomainDefinitionConfiguration());
	}

	protected ConfigurationTypeElement getConverterDefinitionConfiguration() {
		if (converterDefinitionConfigurationValue.isInitialized()) {
			return converterDefinitionConfigurationValue.getValue();
		}

		return converterDefinitionConfigurationValue.setValue(ensureConfigurationContext().getConverterDefinitionConfiguration());
	}

	public List<ConfigurationTypeElement> getConfigurations() {
		return ensureConfigurationContext().getConfigurations();
	}

	protected abstract List<ConfigurationTypeElement> getConfigurationsForType();	
}