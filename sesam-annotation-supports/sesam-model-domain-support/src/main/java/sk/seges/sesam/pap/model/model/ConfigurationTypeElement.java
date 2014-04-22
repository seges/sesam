package sk.seges.sesam.pap.model.model;

import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;

import sk.seges.sesam.core.pap.model.InitializableValue;
import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.pap.model.accessor.KeyAccessor;
import sk.seges.sesam.pap.model.annotation.Ignore;
import sk.seges.sesam.pap.model.annotation.Mapping;
import sk.seges.sesam.pap.model.annotation.Mapping.MappingType;
import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;

public class ConfigurationTypeElement extends TomBaseType {

	private DtoDeclared dtoDeclaredType;
	private boolean dtoTypeElementInitialized = false;

	private InitializableValue<MutableDeclaredType> dtoValue = new InitializableValue<MutableDeclaredType>();

	private DomainDeclaredType domainDeclaredType;
	private boolean domainTypeElementInitialized = false;
	
	protected DomainDeclaredType instantiableDomainType;
	protected boolean instantiableDomainTypeInitialized = false;
	
	private ConfigurationTypeElement delegateConfigurationTypeElement;
	private boolean delegateConfigurationTypeElementInitialized = false;

	private ConverterTypeElement converterTypeElement;
	private boolean converterTypeElementInitialized = false;

	private final Element configurationElement;
	protected final InitializableValue<TransferObjectMappingAccessor> transferObjectConfiguration = new InitializableValue<TransferObjectMappingAccessor>();
	protected boolean initializeTOCReference = false;

	private final MutableDeclaredType domainType;
	protected final MutableDeclaredType dtoType;

	private final String canonicalName;

	protected final ConfigurationContext configurationContext;
	private final InitializableValue<Boolean> hasKey = new InitializableValue<Boolean>();
	
	public ConfigurationTypeElement(MutableDeclaredType domainType, MutableDeclaredType dtoType, Element configurationElement, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext);

		this.configurationContext = configurationContext;
		this.configurationElement = configurationElement;
		this.domainType = domainType;
		this.dtoType = dtoType;
		
		if (configurationElement.getKind().equals(ElementKind.METHOD)) {
			this.canonicalName = configurationElement.getSimpleName().toString();
		} else {
			this.canonicalName = ((TypeElement)configurationElement).getQualifiedName().toString();
		}

		initializeTOCReference = true;
	}
	
	public ConfigurationTypeElement(ExecutableElement configurationElementMethod, DomainDeclaredType returnType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		
		super(envContext);

		this.configurationContext = configurationContext;
		this.configurationElement = configurationElementMethod;
		this.domainType = returnType;
		if (configurationElementMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
			this.dtoType = (MutableDeclaredType) getTypeUtils().toMutableType(configurationElementMethod.getReturnType()); 
		} else {
			this.dtoType = returnType;
		}
		this.canonicalName = configurationElement.getSimpleName().toString();
	}

	public ConfigurationTypeElement(Element configurationElement, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		
		super(envContext);

		this.configurationContext = configurationContext;
		this.configurationElement = configurationElement;
		this.domainType = null;
		this.dtoType = null;

		this.canonicalName = configurationElement.getSimpleName().toString();
	}

	protected TransferObjectMappingAccessor getTransferObjectMappingAccessor() {
		if (!transferObjectConfiguration.isInitialized()) {
			this.transferObjectConfiguration.setValue(
					new TransferObjectMappingAccessor(configurationElement, envContext.getProcessingEnv()));
			if (this.transferObjectConfiguration.getValue().getReferenceMapping() == null) {
				this.transferObjectConfiguration.getValue().setReferenceMapping(transferObjectConfiguration.getValue().getMappingForDto(dtoType));
			}
		}
		return transferObjectConfiguration.getValue();
	}

	@Override
	protected MutableTypeMirror getDelegate() {
		if (configurationElement.getKind().equals(ElementKind.METHOD)) {
			return envContext.getProcessingEnv().getElementUtils().toMutableElement((ExecutableElement) configurationElement).asType();
		}
		return getTypeUtils().toMutableType((DeclaredType)configurationElement.asType());
	}

	public boolean isValid() {
		if (getTransferObjectMappingAccessor() == null) {
			return false;
		}
		
		return getTransferObjectMappingAccessor().isValid();
	}
	
	private ConverterTypeElement getConverter(TransferObjectMappingAccessor transferObjectConfiguration) {
		
		if (!transferObjectConfiguration.isValid()) {
			return null;
		}
		
		TypeElement converter = transferObjectConfiguration.getConverter();
		if (converter != null && transferObjectConfiguration.isConverterGenerated()) {
			return new ConverterTypeElement(this, converter, envContext);
		}
				
		Element configurationElement = asConfigurationElement();
		
		if (!configurationElement.asType().getKind().equals(TypeKind.DECLARED) || !transferObjectConfiguration.isConverterGenerated()) {
			return null;
		}

		TypeElement domain = transferObjectConfiguration.getDomain();

		if (domain == null) {
			return null;
		}

		if (domain.getModifiers().contains(Modifier.ABSTRACT)) {
			return null;
		}

		return getConverterTypeElement();
	}

	public boolean hasKey() {
		if (hasKey.isInitialized()) {
			return hasKey.getValue();
		}

		List<ExecutableElement> methods = ElementFilter.methodsIn(asConfigurationElement().getEnclosedElements());

		for (ExecutableElement method: methods) {
			if (new KeyAccessor(method, envContext.getProcessingEnv()).isValid()) {
				return hasKey.setValue(true);
			}
		}

		return hasKey.setValue(false);
	}

	protected ConverterTypeElement getConverterTypeElement() {
		return new ConverterTypeElement(this, envContext);
	}

	public DomainDeclaredType getRawDomain() {
		if (getDelegateConfigurationTypeElement() != null) {
			return getDelegateConfigurationTypeElement().getRawDomain();
		}

		if (getTransferObjectMappingAccessor().isValid()) {
			TypeElement domainInterface = getTransferObjectMappingAccessor().getDomainInterface();
			if (domainInterface != null) {
				DomainDeclaredType result = getDomain((MutableDeclaredType) getTypeUtils().toMutableType(domainInterface.asType()), dtoType, envContext, configurationContext);
				this.domainDeclaredType.setKind(MutableTypeKind.INTERFACE);
				return result;
			}
		}
		
		return getDomain();
	}

	protected ConfigurationContext getConfigurationContextForDto(MutableDeclaredType dtoType) {
		ConfigurationContext configurationContext = new ConfigurationContext(envContext.getConfigurationEnv());
		List<ConfigurationTypeElement> configurations = envContext.getConfigurationEnv().getConfigurationsForDto(dtoType);
		if (configurations == null || configurations.size() == 0) {
			return null;
		}
		configurationContext.setConfigurations(configurations);
		return configurationContext;
	}
	
	public DomainDeclaredType getDomain() {
		
		if (!this.domainTypeElementInitialized) {
			MutableDeclaredType dtoType = getDtoType();
			
			if (dtoType != null && (configurationElement == null || !configurationElement.getKind().equals(ElementKind.METHOD))) {
				ConfigurationContext configurationContextForDto = getConfigurationContextForDto(dtoType);
				if (configurationContextForDto != null) {
					this.domainDeclaredType = getDomain(configurationContextForDto.getDelegateDomainDefinitionConfiguration(), DomainInstanceType.DEFINITION);
				} else {
					this.domainDeclaredType = getDomain(this, DomainInstanceType.DEFINITION);
				}
			} else {
				this.domainDeclaredType = getDomain(this, DomainInstanceType.DEFINITION);
			}
			this.domainTypeElementInitialized = true;
		}
		
		return domainDeclaredType;
	}

	enum DomainInstanceType {
		INSTANCE, DEFINITION;
	}
	
	private Set<? extends MutableTypeMirror> getInstantiableDomainBounds(Set<? extends MutableTypeMirror> bounds) {
		
		if (bounds == null) {
			return null;
		}
		
		Set<MutableTypeMirror> result = new HashSet<MutableTypeMirror>();
		
		for (MutableTypeMirror bound: bounds) {
			DomainType domainType = envContext.getProcessingEnv().getTransferObjectUtils().getDomainType(bound);
			
			if (domainType.getDomainDefinitionConfiguration() != null) {
				result.add(domainType.getDomainDefinitionConfiguration().getInstantiableDomain());
			} else {
				result.add(bound);
			}
		}
		
		return result;
	}
	
	public DomainDeclaredType getInstantiableDomain() {
		if (!instantiableDomainTypeInitialized) {
			MutableDeclaredType dtoType = getDtoType();
			if (dtoType != null) {
				ConfigurationContext configurationContextForDto = getConfigurationContextForDto(dtoType);
				if (configurationContextForDto != null && configurationContextForDto.getConverterDefinitionConfiguration() != null) {
					this.instantiableDomainType = getDomain(configurationContextForDto.getConverterDefinitionConfiguration(), DomainInstanceType.INSTANCE);
				} else {
					this.instantiableDomainType = getDomain(this, DomainInstanceType.INSTANCE);
				}
			} else {
				this.instantiableDomainType = getDomain(this, DomainInstanceType.INSTANCE);
			}
			
			if (this.instantiableDomainType.getTypeVariables() != null) {
				for (MutableTypeVariable typeVariable: this.instantiableDomainType.getTypeVariables()) {
					typeVariable.setLowerBounds(getInstantiableDomainBounds(typeVariable.getLowerBounds()));
					typeVariable.setUpperBounds(getInstantiableDomainBounds(typeVariable.getUpperBounds()));
				}
			}
			
//			this.instantiableDomainType = getDomain(this);
			this.instantiableDomainTypeInitialized = true;	
		}
		return instantiableDomainType;
	}

	private final DomainDeclaredType getDomain(MutableDeclaredType domainType, MutableDeclaredType dtoType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext, DomainInstanceType domainInstanceType) {
		switch (domainInstanceType) {
		case DEFINITION:
			return getDomain(domainType, dtoType, envContext, configurationContext);
		case INSTANCE:
			return getInstantiableDomain(domainType, dtoType, envContext, configurationContext);
		}
		
		return null;
	}
	
	protected DomainDeclaredType getInstantiableDomain(MutableDeclaredType domainType, MutableDeclaredType dtoType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		return new DomainDeclared(domainType, dtoType, envContext, configurationContext);
	}

	protected DomainDeclaredType getDomain(MutableDeclaredType domainType, MutableDeclaredType dtoType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		return getInstantiableDomain(domainType, dtoType, envContext, configurationContext);
	}

	private DomainDeclaredType getDomain(ConfigurationTypeElement configuration, DomainInstanceType domainInstanceType) {

		DomainDeclaredType domainDeclaredType = null;
		
		MutableDeclaredType domainType = null;
		
		if (configuration.domainType != null) {
			domainType = configuration.domainType;
		} else {
			TypeElement domainTypeElement = configuration.getTransferObjectMappingAccessor().getEvaluatedDomainType();
			if (domainTypeElement != null) {
				domainType = getTypeUtils().toMutableType((DeclaredType) domainTypeElement.asType());
			}
		}

		if (domainType != null) {
			domainDeclaredType = getDomain(domainType, configuration.dtoType, configuration.envContext, configuration.configurationContext, domainInstanceType);
			//domainDeclaredType.prefixTypeParameter(ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX);
		} else {
			if (configuration.domainType == null) {
				TypeElement domainInterface = configuration.getTransferObjectMappingAccessor().getDomainInterface();
				if (domainInterface != null) {
					if (configuration.dtoType != null && getTypeUtils().implementsType(configuration.dtoType, getTypeUtils().toMutableType(domainInterface.asType()))) {
						domainDeclaredType = getDomain(null, configuration.dtoType, configuration.envContext, configuration.configurationContext, domainInstanceType);
					} else {
						domainDeclaredType = getDomain((MutableDeclaredType) getTypeUtils().toMutableType(domainInterface.asType()), 
								configuration.dtoType, configuration.envContext, configuration.configurationContext, domainInstanceType);
						domainDeclaredType.setKind(MutableTypeKind.INTERFACE);
					}
				}
			}
		}

		if (domainDeclaredType == null) {
			configuration.dtoTypeElementInitialized = true;
			configuration.dtoDeclaredType = null;
		}
		
		return domainDeclaredType;
	}

	boolean hasGeneratedDto() {
		return getTransferObjectMappingAccessor().isDtoGenerated() != false && !hasDtoSpecified();
	}

	boolean hasGeneratedConverter() {
		return getTransferObjectMappingAccessor().isConverterGenerated() != false && !hasConverterSpecified();
	}
	
	boolean hasConverterSpecified() {
		return getTransferObjectMappingAccessor().getConverter() != null;
	}

	public boolean hasInstantiableDomainSpecified() {
		return getTransferObjectMappingAccessor().getDomain() != null;
	}

	public boolean hasDtoInterfaceSpecified() {
		return getTransferObjectMappingAccessor().getDtoInterface() != null;
	}

	public TypeElement getDtoSpecified() {
		return getTransferObjectMappingAccessor().getDto();
	}

	public TypeElement getInstantiableDomainSpecified() {
		return getTransferObjectMappingAccessor().getDomain();
	}

	boolean hasDomainSpecified() {
		return getTransferObjectMappingAccessor().getDomain() != null || getTransferObjectMappingAccessor().getDomainInterface() != null;
	}
	
	boolean hasDtoSpecified() {
		return getTransferObjectMappingAccessor().getDto() != null || getTransferObjectMappingAccessor().getDtoInterface() != null;
	}
	
	public DtoDeclaredType getRawDto() {

		if (getTransferObjectMappingAccessor().isValid()) {
			TypeElement dtoInterface = getTransferObjectMappingAccessor().getDtoInterface();
			if (dtoInterface != null) {
				DtoDeclared result = new DtoDeclared(getTypeUtils().toMutableType((DeclaredType) dtoInterface.asType()), envContext, configurationContext);
				result.setInterface(true);
				return result;
			}
		}
		
		DtoDeclaredType dto = getDto();
		
		if (dto != null) {
			return dto;
		}
		
		if (getDelegateConfigurationTypeElement() != null) {
			return getDelegateConfigurationTypeElement().getRawDto();
		}

		return null;
	}
	
	private Set<MutableTypeMirror> getDtoBounds(Set<? extends MutableTypeMirror> bounds) {
	
		Set<MutableTypeMirror> baseBounds = new HashSet<MutableTypeMirror>();
		
		for (MutableTypeMirror lowerBound : bounds) {
			baseBounds.add(envContext.getProcessingEnv().getTransferObjectUtils().getDomainType(lowerBound).getDto());
		}
		
		return baseBounds;
	}

	protected MutableDeclaredType getDtoType() {

		if (dtoValue.isInitialized()) {
			return dtoValue.getValue();
		}

		if (getDelegateConfigurationTypeElement() != null) {
			return dtoValue.setValue(getDelegateConfigurationTypeElement().getDtoType());
		}

		if (this.dtoType != null) {
			return dtoValue.setValue(this.dtoType);
		}

		if (getTransferObjectMappingAccessor().isValid()) {
			TypeElement dtoTypeElement = getTransferObjectMappingAccessor().getDto();
			if (dtoTypeElement != null) {
				return dtoValue.setValue(getTypeUtils().toMutableType((DeclaredType) dtoTypeElement.asType()));
			}

			if (dtoDeclaredType == null) {
				TypeElement dtoInterface = getTransferObjectMappingAccessor().getDtoInterface();
				if (dtoInterface != null) {
					if (domainType != null && getTypeUtils().implementsType(domainType, getTypeUtils().toMutableType(dtoInterface.asType()))) {
						MutableDeclaredType domain = domainType.clone();
						for (MutableTypeVariable typeVariable: domain.getTypeVariables()) {
							typeVariable.setLowerBounds(getDtoBounds(typeVariable.getLowerBounds()));
							typeVariable.setUpperBounds(getDtoBounds(typeVariable.getUpperBounds()));
						}
						return dtoValue.setValue(domain);
					}
					
					return dtoValue.setValue(getTypeUtils().toMutableType((DeclaredType) dtoInterface.asType()));
				}
			}
		}

		if (this.dtoDeclaredType == null) {
			Element configurationElement = asConfigurationElement();
			
			if (!configurationElement.asType().getKind().equals(TypeKind.DECLARED)) {
				return dtoValue.setValue(null);
			}

			return dtoValue.setValue(new DtoDeclared(envContext, new ConfigurationContext(envContext.getConfigurationEnv()).addConfiguration(this)));
		}

		return dtoValue.setValue(dtoDeclaredType);
	}

	public DtoDeclaredType getDto() {
		
		if (!dtoTypeElementInitialized) {

			this.dtoTypeElementInitialized = true;

			MutableDeclaredType dtoType = null;
			
			if (this.dtoType != null) {
				dtoType = this.dtoType;
			} else {
				if (getTransferObjectMappingAccessor().isValid()) {
					TypeElement dtoTypeElement = getTransferObjectMappingAccessor().getDto();
					if (dtoTypeElement != null) {
						dtoType = getTypeUtils().toMutableType((DeclaredType) dtoTypeElement.asType());
					}
				}
			}
			
			if (dtoType != null) {
				this.dtoDeclaredType = new DtoDeclared(dtoType, envContext, configurationContext);
			} else {
				if (this.dtoType == null && getTransferObjectMappingAccessor().isValid()) {
					TypeElement dtoInterface = getTransferObjectMappingAccessor().getDtoInterface();
					if (dtoInterface != null) {
						if (domainType != null && getTypeUtils().implementsType(domainType, getTypeUtils().toMutableType(dtoInterface.asType()))) {
							this.dtoDeclaredType = new DtoDeclared(domainType, envContext, configurationContext);
							this.dtoDeclaredType.initializeDto();
						} else {
							this.dtoDeclaredType = new DtoDeclared(getTypeUtils().toMutableType((DeclaredType) dtoInterface.asType()), envContext, configurationContext);
							this.dtoDeclaredType.setInterface(true);
						}
					}
				}

				if (this.dtoDeclaredType == null) {
					
					if (getDelegateConfigurationTypeElement() != null) {
						this.dtoDeclaredType = (DtoDeclared) getDelegateConfigurationTypeElement().getDto();
					} else {
						Element configurationElement = asConfigurationElement();
						
						if (!configurationElement.asType().getKind().equals(TypeKind.DECLARED)) {
							return null;
						}

						this.dtoDeclaredType = new DtoDeclared(envContext, configurationContext);
						this.dtoDeclaredType.setup();
					}
				}
			}

			if (this.dtoDeclaredType != null && this.dtoType == null) {
				this.dtoDeclaredType.prefixTypeParameter(ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX);
			}
		}
		
		return dtoDeclaredType;
	}
	
	public ConverterTypeElement getConverter() {

		if (!converterTypeElementInitialized || getDomain().hasTypeParameters()) {
			this.converterTypeElement = getConverter(getTransferObjectMappingAccessor());
			this.converterTypeElementInitialized = true;
		}
		
		if (converterTypeElement != null) {
			if (converterTypeElement.isGenerated()) {
				if (getDelegateConfigurationTypeElement() != null) {
					ConverterTypeElement delegateConverterTypeElement = getDelegateConfigurationTypeElement().getConverter();
					if (delegateConverterTypeElement != null) {
						return delegateConverterTypeElement;
					}
				}
			}
			return converterTypeElement;
		}

		if (getDelegateConfigurationTypeElement() != null) {
			return getDelegateConfigurationTypeElement().getConverter();
		}
		
		return null;
	}
	
	public Element asConfigurationElement() {
		return configurationElement;
	}
	
	protected ConfigurationTypeElement getConfigurationTypeElement(Element configurationElement, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		return new ConfigurationTypeElement(getTransferObjectMappingAccessor().getConfiguration(), envContext, configurationContext);
	}
	
	public ConfigurationTypeElement getDelegateConfigurationTypeElement() {

		List<ConfigurationTypeElement> configurations = null;

		if (getTransferObjectMappingAccessor().getConfiguration() != null) {

			configurations = new LinkedList<ConfigurationTypeElement>();

			for (ConfigurationTypeElement configuration: this.configurationContext.getConfigurations()) {
				if (!configuration.isSameType(this)) {
					configurations.add(configuration);
				}
			}
		}

		if (!delegateConfigurationTypeElementInitialized) {
			if (configurations != null) {
				this.delegateConfigurationTypeElement = getConfigurationTypeElement(configurations);
				this.delegateConfigurationTypeElement.configurationContext.addConfiguration(this.delegateConfigurationTypeElement);
			} else {
				this.delegateConfigurationTypeElement = null;
			}
			this.delegateConfigurationTypeElementInitialized = true;
		} else if (configurations != null) {
			//TODO compare also each type in the list
			if (delegateConfigurationTypeElement.configurationContext.getConfigurations().size() != configurations.size()) {
				this.delegateConfigurationTypeElement = getConfigurationTypeElement(configurations);
				this.delegateConfigurationTypeElement.configurationContext.addConfiguration(this.delegateConfigurationTypeElement);
			}
		}

		return delegateConfigurationTypeElement;
	}

	private ConfigurationTypeElement getConfigurationTypeElement(List<ConfigurationTypeElement> configurations) {
		ConfigurationContext configurationContext = new ConfigurationContext(envContext.getConfigurationEnv());
		configurationContext.setConfigurations(configurations);

		return getConfigurationTypeElement(getTransferObjectMappingAccessor().getConfiguration(), envContext, configurationContext);
	}

	public boolean appliesForDomainType(MutableTypeMirror domainType) {
		
		//Configuration should be applied only for declared types like classes or interfaces
		if (!domainType.getKind().equals(MutableTypeKind.CLASS) &&
			!domainType.getKind().equals(MutableTypeKind.INTERFACE)) {
			return false;
		}
		
		return getTransferObjectMappingAccessor().getMappingForDomain((MutableDeclaredType) domainType) != null;
	}

	public boolean appliesForDtoType(MutableTypeMirror dtoType) {

		//Configuration should be applied only for declared types like classes or interfaces
		if (!dtoType.getKind().equals(MutableTypeKind.CLASS) &&
			!dtoType.getKind().equals(MutableTypeKind.INTERFACE)) {
				return false;
		}
		
		TransferObjectMapping mappingForDto = getTransferObjectMappingAccessor().getMappingForDto((MutableDeclaredType) dtoType);
		
		if (mappingForDto != null) {
			return true;
		}

		if (this.dtoType == null) {
			String dtoClassName = getDtoType().getCanonicalName();
			return (dtoClassName != null && dtoClassName.equals(dtoType.toString(ClassSerializer.CANONICAL, false)));
		}
		
		return false;
	}

	public String getCanonicalName() {
		return canonicalName;
	}
	
	public MappingType getMappingType() {
		MappingType mappingType = MappingType.AUTOMATIC;
		Mapping mapping =  configurationElement.getAnnotation(Mapping.class);

		if (mapping != null) {
			mappingType = mapping.value();
		}
		
		return mappingType;
	}

	boolean isFieldIgnored(String field) {

		List<ExecutableElement> overridenMethods = ElementFilter.methodsIn(asConfigurationElement().getEnclosedElements());

		for (ExecutableElement overridenMethod : overridenMethods) {

			if (MethodHelper.toField(overridenMethod).equals(field)) {
				Ignore ignoreAnnotation = overridenMethod.getAnnotation(Ignore.class);
				if (ignoreAnnotation != null) {
					return true;
				}
			}
		}

		return false;
	}
}