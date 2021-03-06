package sk.seges.sesam.pap.model.model;

import sk.seges.sesam.core.pap.model.InitializableValue;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableExecutableElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableElements;
import sk.seges.sesam.core.pap.structure.DefaultPackageValidator.ImplementationType;
import sk.seges.sesam.core.pap.structure.DefaultPackageValidator.LayerType;
import sk.seges.sesam.core.pap.structure.DefaultPackageValidator.LocationType;
import sk.seges.sesam.core.pap.structure.DefaultPackageValidatorProvider;
import sk.seges.sesam.core.pap.structure.api.PackageValidator;
import sk.seges.sesam.core.pap.structure.api.PackageValidatorProvider;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.pap.model.annotation.Ignore;
import sk.seges.sesam.pap.model.model.api.GeneratedClass;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;
import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.Serializable;
import java.util.*;

class DtoDeclared extends TomDeclaredConfigurationHolder implements GeneratedClass, DtoDeclaredType {

	private final boolean generated;
	private final MutableDeclaredType dtoType;

	private InitializableValue<DomainDeclaredType> domainType = new InitializableValue<DomainDeclaredType>();
	private InitializableValue<ConverterTypeElement> converterType = new InitializableValue<ConverterTypeElement>();
	private InitializableValue<MutableExecutableElement> idMethod = new InitializableValue<MutableExecutableElement>();

	private boolean isInterface = false;
	
	DtoDeclared(MutableDeclaredType dtoType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);

		this.dtoType = dtoType;
		this.generated = false;
	}

	DtoDeclared(DeclaredType dtoType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);

		this.dtoType = getTypeUtils().toMutableType(dtoType);
		this.generated = false;
	}

	
	DtoDeclared(EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);

		this.generated = true;
		this.dtoType = null;
	}
		
	void setup() {
		setKind(MutableTypeKind.CLASS);
		
		DomainDeclaredType superClassDomainType = getInstantiableDomain().getSuperClass();

		if (superClassDomainType != null) {
			DtoDeclaredType superclassDto = superClassDomainType.getDto();

			if (superclassDto != null && !superclassDto.getKind().equals(MutableTypeKind.INTERFACE)) {
				//TODO Possibly add interface here!
				MutableDeclaredType mutableSuperclassType = getTypeUtils().toMutableType((DeclaredType)superClassDomainType.asConfigurationElement().asType());
				
				if (superclassDto instanceof MutableDeclaredType) {
					//TODO convert type variables also
					superclassDto.setTypeVariables(mutableSuperclassType.getTypeVariables().toArray(new MutableTypeVariable[] {}));
					setSuperClass(superclassDto);
				} else {
					//TODO log something here
				}
			}
		}

		ConfigurationTypeElement domainDefinitionConfiguration = getDomainDefinitionConfiguration();
		
		if (domainDefinitionConfiguration != null && domainDefinitionConfiguration.asConfigurationElement().asType().getKind().equals(TypeKind.DECLARED)) {
			List<? extends TypeMirror> interfaces = ((TypeElement)domainDefinitionConfiguration.asConfigurationElement()).getInterfaces();
			
			List<MutableTypeMirror> interfaceTypes = new ArrayList<MutableTypeMirror>();
			
			if (interfaces != null) {
				for (TypeMirror interfaceType: interfaces) {
					MutableDeclaredType mutableType = (MutableDeclaredType) getTypeUtils().toMutableType(interfaceType);
					mutableType.prefixTypeParameter(ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX);
					List<? extends MutableTypeVariable> typeVariables = mutableType.getTypeVariables();
					for (MutableTypeVariable typeVariable: typeVariables) {
						typeVariable.setLowerBounds(converterTypesToDto(typeVariable.getLowerBounds()));
						typeVariable.setUpperBounds(converterTypesToDto(typeVariable.getUpperBounds()));
					}
					interfaceTypes.add(mutableType);
				}
			}
			
			MutableTypeMirror superClass = super.getSuperClass();
			if (superClass == null) {
				if (domainDefinitionConfiguration.ensureDelegateType().getKind().isDeclared() && ((MutableDeclaredType) domainDefinitionConfiguration.ensureDelegateType()).getSuperClass() != null) {
					setSuperClass(((MutableDeclaredType) domainDefinitionConfiguration.ensureDelegateType()).getSuperClass());
				} else {
					//TODO check if it is not already there
					interfaceTypes.add(getTypeUtils().toMutableType(Serializable.class));
				}
			}
			
			setInterfaces(interfaceTypes);
		}
	}

	private Set<? extends MutableTypeMirror> converterTypesToDto(Set<? extends MutableTypeMirror> types) {
		if (types == null) {
			return null;
		}
		Set<MutableTypeMirror> result = new LinkedHashSet<MutableTypeMirror>();
		
		for (MutableTypeMirror type: types) {
			result.add(environmentContext.getProcessingEnv().getTransferObjectUtils().getDomainType(type).getDto());
		}
		return result;
	}
	
	DtoDeclared(PrimitiveType dtoType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);

		this.dtoType = (MutableDeclaredType) getTypeUtils().toMutableType(dtoType);
		this.generated = false;
	}

	protected List<ConfigurationTypeElement> getConfigurationsForType() {
		return getConfigurations(this.dtoType);
	};

	private List<ConfigurationTypeElement> getConfigurations(MutableTypeMirror dtoType) {
		return environmentContext.getConfigurationEnv().getConfigurationsForDto(dtoType);
	}
		
	protected MutableDeclaredType getDelegate() {
		if (dtoType != null) {
			return (MutableDeclaredType) getTypeUtils().toMutableType(dtoType);
		}
		return getGeneratedDtoTypeFromConfiguration();
	};

	@Override
	public ConfigurationTypeElement getDomainDefinitionConfiguration() {
		return ensureConfigurationContext().getDelegateDomainDefinitionConfiguration();
	}

	private MutableTypeMirror[] getDomainBounds(Set<? extends MutableTypeMirror> bounds) {
		Set<MutableTypeMirror> domainBounds = new HashSet<MutableTypeMirror>();
		for (MutableTypeMirror bound: bounds) {
			domainBounds.add(getTransferObjectUtils().getDtoType(bound).getDomain());
		}
		
		return domainBounds.toArray(new MutableTypeMirror[] {});
	}
	
	private void initializeDomain(DomainDeclaredType domain) {
		if (getConfigurations().size() == 0 || dtoType == null || dtoType.getTypeVariables() == null) {
			return;
		}

		List<MutableTypeVariable> typeVariables = new ArrayList<MutableTypeVariable>();
		
		for (MutableTypeVariable typeVariable: dtoType.getTypeVariables()) {
			typeVariables.add(environmentContext.getProcessingEnv().getTypeUtils().getTypeVariable(typeVariable.getVariable(), 
					getDomainBounds(typeVariable.getLowerBounds()), getDomainBounds(typeVariable.getUpperBounds())));
		}
		
		domain.setTypeVariables(typeVariables.toArray(new MutableTypeVariable[] {}));
	}
	
	public void initializeDto() {
		
		if (getConfigurations().size() == 0) {
			return;
		}

		ConfigurationTypeElement domainDefinitionConfiguration = getDomainDefinitionConfiguration();

		if (domainDefinitionConfiguration.getDomain().hasTypeParameters()) {
		
			List<? extends TypeMirror> typeArguments = ((DeclaredType)domainDefinitionConfiguration.getDomain().asType()).getTypeArguments();
			List<MutableTypeVariable> dtoTypeVariables = new LinkedList<MutableTypeVariable>();
			if (typeArguments.size() == getTypeVariables().size()) {
				for (TypeMirror domainTypeVariable: typeArguments) {
					DtoType dtoTypeVariable = getTransferObjectUtils().getDomainType(domainTypeVariable).getDto();
					if (dtoTypeVariable instanceof MutableTypeVariable) {
						dtoTypeVariables.add((MutableTypeVariable)dtoTypeVariable);
					} else {
						dtoTypeVariables.add(getTypeUtils().getTypeVariable(null, dtoTypeVariable));
					}
				}
			}
			setDelegate(getDelegate().clone().setTypeVariables(dtoTypeVariables.toArray(new MutableTypeVariable[] {})).stripTypeParametersTypes());
		}
	}
	
	protected PackageValidatorProvider getPackageValidationProvider() {
		return new DefaultPackageValidatorProvider();
	}

	private InitializableValue<MutableDeclaredType> generatedDtoTypeFromConfiguration = new InitializableValue<MutableDeclaredType>();

	private MutableDeclaredType getGeneratedDtoTypeFromConfiguration() {

		if (this.generatedDtoTypeFromConfiguration.isInitialized()) {
			return generatedDtoTypeFromConfiguration.getValue();
		} else {

			MutableDeclaredType outputType = ((MutableDeclaredType) getDomainDefinitionConfiguration().ensureDelegateType()).clone();

			PackageValidator packageValidator = getPackageValidationProvider().get(outputType)
					.moveTo(LocationType.SHARED).moveTo(LayerType.MODEL).clearType().moveTo(ImplementationType.DTO);
			outputType = outputType.changePackage(packageValidator).setKind(MutableTypeKind.CLASS);
			return generatedDtoTypeFromConfiguration.setValue(outputType.getSimpleName().endsWith(TransferObjectHelper.DEFAULT_SUFFIX) ? outputType.setSimpleName(outputType
					.getSimpleName().substring(0, outputType.getSimpleName().length() - TransferObjectHelper.DEFAULT_SUFFIX.length()))
					: outputType.addClassSufix(TransferObjectHelper.DTO_SUFFIX));
		}
	}

	@Override
	public boolean isGenerated() {
		return generated;
	}
	
	public DomainDeclaredType getDomain() {
		if (!this.domainType.isInitialized()) {
			ConfigurationTypeElement domainDefinitionConfiguration = getDomainDefinitionConfiguration();
			
			if (domainDefinitionConfiguration != null) {
				initializeDomain(this.domainType.setValue(domainDefinitionConfiguration.getDomain()));
			}
			
			if (this.domainType.getValue() == null && dtoType != null) {
				this.domainType.setValue((DomainDeclaredType) getTransferObjectUtils().getDomainType(dtoType.clone()));
			}

			this.domainType.setInitialized();
		}

		return this.domainType.getValue();
	}

	public DomainDeclaredType getInstantiableDomain() {
		ConfigurationTypeElement domainDefinitionConfiguration = getDomainDefinitionConfiguration();
			
		if (domainDefinitionConfiguration != null) {
			return domainDefinitionConfiguration.getInstantiableDomain();
		}
			
		return getDomain();
	}

	public DtoDeclared getSuperClass() {

		MutableDeclaredType superClassDelegate = ensureDelegateType().getSuperClass();
		if (superClassDelegate != null) {
			return (DtoDeclared) getTransferObjectUtils().getDtoType(superClassDelegate);
		}

		return null;
	}
	
	public boolean isInterface() {
		return isInterface;
	}
	
	void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}
		
	public ConverterTypeElement getConverter() {
		if (!this.converterType.isInitialized() || hasTypeParameters()) {
	 		ConfigurationTypeElement converterDefinitionConfiguration = getConverterDefinitionConfiguration();
			
			if (converterDefinitionConfiguration != null) {
				return this.converterType.setValue(converterDefinitionConfiguration.getConverter());
			}

			DtoDeclared dtoType = (DtoDeclared) environmentContext.getProcessingEnv().getTransferObjectUtils().getDtoType(
					environmentContext.getProcessingEnv().getTypeUtils().getDeclaredType(this, new MutableDeclaredType[] {}));
			if (dtoType.getConverterDefinitionConfiguration() != null) {
				return this.converterType.setValue(dtoType.getConverter());
			}
			
			this.converterType.setInitialized();
		}
		
		return converterType.getValue();
	}

	private MutableElements getElements() {
		return environmentContext.getProcessingEnv().getElementUtils();
	}
	
	private MutableExecutableElement toMutableMethod(ExecutableElement domainMethod) {
		MutableExecutableElement dtoMethod = getElements().toMutableElement(domainMethod);
		dtoMethod.asType().setReturnType(getTransferObjectUtils().getDomainType(domainMethod.getReturnType()).getDto());
		return dtoMethod;
	}
	
	public MutableExecutableElement getIdMethod(EntityResolver entityResolver) {

		if (!this.idMethod.isInitialized()) {
			ConfigurationTypeElement converterDefinitionConfiguration = getConverterDefinitionConfiguration(); //getDomainDefinitionConfiguration();

			if (converterDefinitionConfiguration != null) {
				DtoDeclared dtoType = (DtoDeclared) environmentContext.getProcessingEnv().getTransferObjectUtils().getDtoType(this);
				if (dtoType.getConverterDefinitionConfiguration() != null) {
					converterDefinitionConfiguration = dtoType.getConverterDefinitionConfiguration();
				}
			}
			
			if (converterDefinitionConfiguration == null) {
				converterDefinitionConfiguration = getDomainDefinitionConfiguration();
			}
			
			if (converterDefinitionConfiguration != null) {
				List<ExecutableElement> overridenMethods = ElementFilter.methodsIn(converterDefinitionConfiguration.asConfigurationElement().getEnclosedElements());
		
				DomainDeclaredType domainType = converterDefinitionConfiguration.getDomain();
		
				if (!domainType.asType().getKind().equals(TypeKind.DECLARED)) {
					return this.idMethod.setValue(null);
				}
				
				for (ExecutableElement overridenMethod : overridenMethods) {
		
					Ignore ignoreAnnotation = overridenMethod.getAnnotation(Ignore.class);
					if (ignoreAnnotation == null) {
		
						if (entityResolver.isIdMethod(overridenMethod)) {
							if (overridenMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
								return this.idMethod.setValue(toMutableMethod(domainType.getGetterMethod(TransferObjectHelper.getFieldPath(overridenMethod))));
							} else {
								return this.idMethod.setValue(getElements().toMutableElement(overridenMethod));
							}
						}
					}
				}
			}
			
			ExecutableElement idMethod = getInstantiableDomain().getIdMethod(entityResolver);
			
			if (converterDefinitionConfiguration != null) {
				if (idMethod != null && !converterDefinitionConfiguration.isFieldIgnored(MethodHelper.toField(idMethod))) {
					return this.idMethod.setValue(toMutableMethod(idMethod));
				}
			}
			
			if (idMethod != null) {
				return this.idMethod.setValue(getElements().toMutableElement(idMethod));
			}
			
			return this.idMethod.setValue(null);
		}
		
		return this.idMethod.getValue();
	}
}