package sk.seges.sesam.pap.model.model;

import sk.seges.sesam.core.pap.model.InitializableValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigurationContext {

	private List<ConfigurationTypeElement> configurations = new ArrayList<ConfigurationTypeElement>();
	private final ConfigurationEnvironment env;
	
	public ConfigurationContext(ConfigurationEnvironment env) {
		this.env = env;
	}

	public ConfigurationContext addConfiguration(ConfigurationTypeElement configuration) {
		configurations.add(configuration);
		return this;
	}
	
	public void setConfigurations(List<ConfigurationTypeElement> configurations) {
		this.configurations = configurations;
	}
	
	public ConfigurationEnvironment getEnv() {
		return env;
	}
	
	public List<ConfigurationTypeElement> getConfigurations() {
		return Collections.unmodifiableList(configurations);
	}
		
	ConfigurationTypeElement ensureConfiguration(TomType... tomTypes) {
		ConfigurationTypeElement configuration = getConfiguration(tomTypes);
		
		if (configuration != null) {
			return configuration;
		}
		
		if (configurations.size() == 0) {
			return null;
		}
		
		return configurations.get(0);
	}
	
	ConfigurationTypeElement getConfiguration(TomType... tomTypes) {
		for (ConfigurationTypeElement configuration: configurations) {
			if (TomType.appliesFor(configuration, tomTypes)) {
				return configuration;
			}
		}

		return null;
	}

	private InitializableValue<ConfigurationTypeElement> domainDefinitionConfigurationValue = new InitializableValue<ConfigurationTypeElement>();
	private InitializableValue<ConfigurationTypeElement> converterDefinitionConfigurationValue = new InitializableValue<ConfigurationTypeElement>();
	private InitializableValue<ConfigurationTypeElement> delegateDomainDefinitionConfigurationValue = new InitializableValue<ConfigurationTypeElement>();

	public ConfigurationTypeElement getDelegateDomainDefinitionConfiguration() {

		if (delegateDomainDefinitionConfigurationValue.isInitialized()) {
			return delegateDomainDefinitionConfigurationValue.getValue();
		}

		ConfigurationTypeElement domainDefinitionConfiguration = getDomainDefinitionConfiguration();

		if (domainDefinitionConfiguration != null && domainDefinitionConfiguration.getDelegateConfigurationTypeElement() != null) {
			domainDefinitionConfiguration = domainDefinitionConfiguration.getDelegateConfigurationTypeElement();
		}
		
		return delegateDomainDefinitionConfigurationValue.setValue(domainDefinitionConfiguration);
	}

	ConfigurationTypeElement getDomainDefinitionConfiguration() {

		if (domainDefinitionConfigurationValue.isInitialized()) {
			return domainDefinitionConfigurationValue.getValue();
		}

		ConfigurationTypeElement configuration = getConfiguration(TomType.DOMAIN, TomType.DTO_NOT_DEFINED);
		if (configuration != null) {
			return domainDefinitionConfigurationValue.setValue(configuration);
		}
		return domainDefinitionConfigurationValue.setValue(ensureConfiguration(TomType.DOMAIN));
	}

	ConfigurationTypeElement getConverterDefinitionConfiguration() {

		if (converterDefinitionConfigurationValue.isInitialized()) {
			return converterDefinitionConfigurationValue.getValue();
		}

		ConfigurationTypeElement configuration = getConfiguration(TomType.CONVERTER_DEFINED);
		if (configuration != null) {
			return converterDefinitionConfigurationValue.setValue(configuration);
		}
		configuration = getConfiguration(TomType.CONVERTER_GENERATED);
		if (configuration != null) {
			return converterDefinitionConfigurationValue.setValue(configuration);
		}

		ConfigurationTypeElement delegateConfiguration = getConfiguration(TomType.DELEGATE);
		
		if (delegateConfiguration != null) {
			return converterDefinitionConfigurationValue.setValue(
					delegateConfiguration.getDelegateConfigurationTypeElement().configurationContext.getConverterDefinitionConfiguration());
		}

		return converterDefinitionConfigurationValue.setValue(null);
	}
}