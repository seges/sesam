package sk.seges.sesam.pap.model.model;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.model.InitializableValue;
import sk.seges.sesam.core.pap.model.PathResolver;
import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.utils.MethodHelper.MethodType;
import sk.seges.sesam.pap.model.annotation.Mapping.MappingType;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.domain.DomainTypeVariable;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.provider.api.ConfigurationProvider;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;
import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

public class DomainDeclared extends TomDeclaredConfigurationHolder implements DomainDeclaredType {

	protected MutableDeclaredType domainType;
	protected MutableDeclaredType dtoType;

	private InitializableValue<DomainDeclaredType> superClassDomainTypeValue = new InitializableValue<DomainDeclaredType>();

	private boolean idMethodInitialized = false;
	private ExecutableElement idMethod;

	public DomainDeclared(MutableDeclaredType domainType, MutableDeclaredType dtoType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);
		
		this.domainType = domainType;
		this.dtoType = dtoType;

		initialize();
	}

	/**
	 * When there is only one configuration for domain type, it is OK ... but for multiple DTOs created from one domain type it can
	 * lead to unexpected results
	 */
	@Deprecated
	public DomainDeclared(DeclaredType domainType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);
		
		this.domainType = envContext.getProcessingEnv().getTypeUtils().toMutableType(domainType);
		this.dtoType = null;

	}

	/**
	 * When there is only one configuration for domain type, it is OK ... but for multiple DTOs created from one domain type it can
	 * leads to unexpected results
	 */
	@Deprecated
	public DomainDeclared(PrimitiveType domainType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);
		
		this.domainType = (MutableDeclaredType) envContext.getProcessingEnv().getTypeUtils().toMutableType(domainType);
		this.dtoType = null;
	}

	/**
	 * When there is only one configuration for domain type, it is OK ... but for multiple DTOs created from one domain type it can
	 * leads to unexpected results
	 */
	@Deprecated
	public DomainDeclared(MutableDeclaredType domainType, EnvironmentContext<TransferObjectProcessingEnvironment> envContext, ConfigurationContext configurationContext) {
		super(envContext, configurationContext);
		
		this.domainType = domainType;
		this.dtoType = null;
	}


	@Override
	protected List<ConfigurationTypeElement> getConfigurationsForType() {
		return getConfigurations(domainType);
	}
	
	protected List<ConfigurationTypeElement> getConfigurations(MutableTypeMirror domainType) {
		for (ConfigurationProvider configurationProvider: getConfigurationProviders()) {
			List<ConfigurationTypeElement> configurationsForDomain = configurationProvider.getConfigurationsForDomain(domainType);
			if (configurationsForDomain != null && configurationsForDomain.size() > 0) {
				return configurationsForDomain;
			}
		}
		
		return new ArrayList<ConfigurationTypeElement>();
	}

	@Override
	public ConverterTypeElement getConverter() {
		return getDto().getConverter();
	}

	@Override
	public DtoDeclaredType getDto() {
		ConfigurationTypeElement domainDefinitionConfiguration = getDomainDefinitionConfiguration();
		
		if (domainDefinitionConfiguration == null) {
			if (domainType != null) {
				return new DtoDeclared(domainType.clone(), environmentContext, ensureConfigurationContext());
			}
			
			return null;
		}
		
		return domainDefinitionConfiguration.getDto();
	}

	public ExecutableElement getIdMethod(EntityResolver entityResolver) {
		
		if (!idMethodInitialized) {
			List<ExecutableElement> overridenMethods =  new ArrayList<ExecutableElement>();
			
			ConfigurationTypeElement domainDefinitionConfiguration = getDomainDefinitionConfiguration();

			if (domainDefinitionConfiguration != null) {
				overridenMethods = ElementFilter.methodsIn(domainDefinitionConfiguration.asConfigurationElement().getEnclosedElements());
			}
			
			MappingType mappingType = MappingType.AUTOMATIC;
			if (domainDefinitionConfiguration != null) {
				mappingType = domainDefinitionConfiguration.getMappingType();
			}
			
			for (ExecutableElement overridenMethod: overridenMethods) {
	
				String fieldName = TransferObjectHelper.getFieldPath(overridenMethod);
				
				ExecutableElement domainMethod = getGetterMethod(fieldName);
	
				if (domainMethod != null && !new PathResolver(fieldName).isNested() && entityResolver.isIdMethod(domainMethod)) {
					if (this.idMethod != null && (this.idMethod.getParameters().size() != domainMethod.getParameters().size() || 
												 !this.idMethod.getReturnType().equals(domainMethod.getReturnType()))) {
						getMessager().printMessage(Kind.ERROR, "[ERROR] Multiple identifier methods were specified." + 
								this.idMethod.getSimpleName().toString() + " in the " + this.idMethod.getEnclosingElement().toString() + " class and " +
								domainMethod.getSimpleName().toString() + " in the configuration " + domainMethod.getEnclosingElement().toString(), 
								domainDefinitionConfiguration.asConfigurationElement());
					}
					if (this.idMethod == null) {
						this.idMethod = domainMethod;
					}
				}
			}
	
			TypeElement processingElement = asConfigurationElement();
			
			findIdMethod(processingElement, mappingType, domainDefinitionConfiguration, entityResolver);
			
			for (ExecutableElement overridenMethod: overridenMethods) {
				if (entityResolver.isIdMethod(overridenMethod)) {
					if (this.idMethod != null && (this.idMethod.getParameters().size() != overridenMethod.getParameters().size() || 
							  					 !this.idMethod.getReturnType().equals(overridenMethod.getReturnType()))) {
						handleMultipleIdentifiers(overridenMethod, domainDefinitionConfiguration);
					}
					
					if (this.idMethod == null) {
						idMethod = overridenMethod;
					}
				}
			}
			
			if (this.idMethod == null) {
				findIdMethod(processingElement, MappingType.AUTOMATIC, domainDefinitionConfiguration, entityResolver);
			}
			
			idMethodInitialized = true;
		}
		
		return idMethod;
	}
	
	private void findIdMethod(TypeElement processingElement, MappingType mappingType, ConfigurationTypeElement domainDefinitionConfiguration, EntityResolver entityResolver) {

		while (processingElement != null) {
			
			List<ExecutableElement> methods = ElementFilter.methodsIn(processingElement.getEnclosedElements());
	
			if (mappingType.equals(MappingType.AUTOMATIC)) {
				for (ExecutableElement method: methods) {
					//TODO Setter for ID is also required?!
					if (MethodHelper.isGetterMethod(method) && /*pojoElement.hasSetterMethod(method) && */ method.getModifiers().contains(Modifier.PUBLIC) && entityResolver.isIdMethod(method)) {
						if (this.idMethod != null && (this.idMethod.getParameters().size() != idMethod.getParameters().size() || 
				 					 				 !this.idMethod.getReturnType().equals(idMethod.getReturnType()))) {
							handleMultipleIdentifiers(method, domainDefinitionConfiguration);
						}
						if (this.idMethod == null) {
							this.idMethod = method;
						}
					}
				}

				for (TypeMirror interfaceType : processingElement.getInterfaces()) {
					if (interfaceType.getKind().equals(TypeKind.DECLARED)) {
						ExecutableElement idMethod = ((DomainDeclared)getDomainForType(interfaceType)).getIdMethod(entityResolver);
						if (idMethod != null) {
							if (this.idMethod != null && (this.idMethod.getParameters().size() != idMethod.getParameters().size() || 
				  					 					 !this.idMethod.getReturnType().equals(idMethod.getReturnType()))) {
								handleMultipleIdentifiers(idMethod, domainDefinitionConfiguration);
							}
							if (this.idMethod == null) {
								this.idMethod = idMethod;
							}
						}
					}
				}
			}
							
			if (processingElement.getSuperclass() != null && processingElement.getSuperclass().getKind().equals(TypeKind.DECLARED)) {
				processingElement = (TypeElement)((DeclaredType)processingElement.getSuperclass()).asElement();
			} else {
				processingElement = null;
			}
		}
	}

	private void handleMultipleIdentifiers(ExecutableElement method, ConfigurationTypeElement domainDefinitionConfiguration) {
		getMessager().printMessage(Kind.ERROR, "[ERROR] Multiple identifier methods were specified." + 
				this.idMethod.getSimpleName().toString() + " in the " + this.idMethod.getEnclosingElement().toString() + " class and " +
				method.getSimpleName().toString() + " in the configuration " + method.getEnclosingElement().toString(), 
				domainDefinitionConfiguration.asConfigurationElement());
	}
	
	public DomainType getDomainReference(EntityResolver entityResolver, String fieldName) {
		ExecutableElement getterMethod = getGetterMethod(fieldName);
		if (getterMethod == null) {
			return null;
		}
		
		if (!getterMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
			return null;
		}
		
		TypeMirror targetEntityType = entityResolver.getTargetEntityType(getterMethod);
		
		return getDomainForType(targetEntityType);
	}
	
	public ExecutableElement getGetterMethod(String fieldName) {
		return getMethod(new PathResolver(fieldName), MethodType.GETTER, true);
	}
	
//	public ExecutableElement getIsGetterMethod(String fieldName) {
//		return getMethod(new PathResolver(fieldName), MethodHelper.GETTER_IS_PREFIX, true);
//	}

	public ExecutableElement getSetterMethod(String fieldName) {
		return getMethod(new PathResolver(fieldName), MethodType.SETTER, true);
	}

	public ExecutableElement getMethod(String fieldName, MethodHelper.MethodType type) {
		return getMethod(new PathResolver(fieldName), type, true);
	}
	
	private ExecutableElement getMethod(PathResolver pathResolver, MethodHelper.MethodType type, boolean searchInstantiableType) {

		if (!pathResolver.hasNext()) {
			return null;
		}

		List<ExecutableElement> methods;
		
		if (getDomainDefinitionConfiguration() != null && searchInstantiableType) {
			methods = ElementFilter.methodsIn(getDomainDefinitionConfiguration().getInstantiableDomain().asConfigurationElement().getEnclosedElements());
		} else {
			methods = ElementFilter.methodsIn(asConfigurationElement().getEnclosedElements());
		}

		String fieldName = pathResolver.next();

		MethodType currentType = MethodType.GETTER;

		if (!pathResolver.hasNext()) {
			currentType = type;
		}

		for (ExecutableElement elementMethod : methods) {

			if (elementMethod.getModifiers().contains(Modifier.PUBLIC) && currentType.isMethodOfType(elementMethod) && 
					MethodHelper.toField(elementMethod).equals(fieldName)) {

				if (!pathResolver.hasNext()) {
					return elementMethod;
				}

				if (elementMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
					return ((DomainDeclared)getDomainForType(elementMethod.getReturnType())).getMethod(pathResolver.next(), type);
				}

				// incompatible types - nested path is expected, but declared
				// type was not found
				getMessager().printMessage(Kind.WARNING,
						"incompatible types - nested path (" + fieldName + ") is expected, but declared type was not found ", asConfigurationElement());
				return null;
			}
		}

		if (asConfigurationElement().getKind().equals(ElementKind.CLASS) || asConfigurationElement().getKind().equals(ElementKind.INTERFACE)) {

			TypeElement typeElement = (TypeElement) asConfigurationElement();
			if (typeElement.getSuperclass() != null && typeElement.getSuperclass().getKind().equals(TypeKind.DECLARED)) {
				pathResolver.reset();
				DomainType domainType = getDomainForType(typeElement.getSuperclass());
				ExecutableElement method = ((DomainDeclared)domainType).getMethod(pathResolver, type, false);
				if (method != null) {
					return method;
				}
			}
			
			List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
			for (TypeMirror interfaceType: interfaces) {
				pathResolver.reset();
				DomainType domainType = getDomainForType(interfaceType);
				ExecutableElement method = ((DomainDeclared)domainType).getMethod(pathResolver, type, false);
				if (method != null) {
					return method;
				}
			}
		}

		return null;
	}

	public DomainType getId(EntityResolver entityResolver) {
		ExecutableElement idMethod = getIdMethod(entityResolver);
		
		if (idMethod == null) {
			return null;
		}
		
		return getDomainForType(idMethod.getReturnType());
	}

	@Override
	public DomainDeclaredType getSuperClass() {
		TypeMirror type = asType();
		
		if (type == null) {
			TypeElement typeElement = environmentContext.getProcessingEnv().getElementUtils().getTypeElement(getCanonicalName());
			if (typeElement == null) {
				return null;
			}
			type = typeElement.asType();
		}
		
		if (!type.getKind().equals(TypeKind.DECLARED)) {
			return null;
		}
		
		Element element = ((DeclaredType)type).asElement();
		
		if (element.getKind().equals(ElementKind.CLASS) || element.getKind().equals(ElementKind.INTERFACE)) {
			TypeElement typeElement = (TypeElement)element;
			if (typeElement.getSuperclass() != null && typeElement.getSuperclass().getKind().equals(TypeKind.DECLARED)) {
				if (!superClassDomainTypeValue.isInitialized()) {
					TypeMirror domainSuperClass = typeElement.getSuperclass();
					
					List<ConfigurationTypeElement> configurationElements = getConfigurations(getTypeUtils().toMutableType(domainSuperClass));

					if (configurationElements != null && configurationElements.size() > 0) {
						for (ConfigurationTypeElement configurationElement: configurationElements) {
							if (configurationElement.hasInstantiableDomainSpecified() &&
									!configurationElement.getInstantiableDomain().toString(ClassSerializer.CANONICAL, false).equals(
											this.toString(ClassSerializer.CANONICAL, false)) &&
								environmentContext.getProcessingEnv().getTypeUtils().isAssignable(domainSuperClass, configurationElement.getInstantiableDomain().asType())) {

								//it can be the same if base class is the case as instance class
								//usable for - when DTO is created from base class and converter from instance class
								superClassDomainTypeValue.setValue(configurationElement.getInstantiableDomain());
								break;
							}
						}
					} else {
						environmentContext.getProcessingEnv().getMessager().printMessage(Kind.OTHER, "No configuration for " + domainSuperClass + " was found. Inheritance is not supported for " + element);
					}
					
					if (!superClassDomainTypeValue.isInitialized()) {
						superClassDomainTypeValue.setInitialized();
					}
				}
				return superClassDomainTypeValue.getValue();
			}
		}
		
		return null;
	}

	
	@Override
	protected MutableDeclaredType getDelegate() {
		if (domainType != null) {
			return domainType.clone();
		}
		return dtoType.clone();
	}

	protected DomainType getDomainForType(TypeMirror type) {
		DomainType domainType = getTransferObjectUtils().getDomainType(type);

		if (domainType != null) {
			return domainType;
		}
		
		if (type.getKind().isPrimitive()) {
			return new DomainDeclared((PrimitiveType)type, environmentContext, ensureConfigurationContext());
		}
		
		if (type.getKind().equals(TypeKind.DECLARED)) {
			return new DomainDeclared((DeclaredType)type, environmentContext, ensureConfigurationContext());
		}
		
		return null;
	}

	private void initialize() {
		if (dtoType != null && dtoType.getTypeVariables().size() > 0) {

			MutableTypeVariable[] typeVariables = new MutableTypeVariable[dtoType.getTypeVariables().size()];
			
			int i = 0;
			for (MutableTypeVariable typeVariable: dtoType.getTypeVariables()) {
				typeVariables[i++] = (DomainTypeVariable)getTransferObjectUtils().getDtoType(typeVariable.clone()).getDomain();
			}
			
			MutableDeclaredType result = setTypeVariables(typeVariables);
			stripTypeParametersTypes();
			this.domainType = result;
		} else if (dtoType != null && domainType == null) {
			this.domainType = dtoType.clone();
		}
	}

	//TODO rename to asDataElement, Data ?
	public TypeElement asConfigurationElement() {
		//TODO it is
		if (asType().getKind().equals(TypeKind.DECLARED)) {
			return (TypeElement)((DeclaredType)asType()).asElement();
		}
		return null;
	}

	public TypeMirror asType() {
		return getTypeUtils().fromMutableType(domainType);
	}

	@Override
	public MutableDeclaredType asMutable() {
		return ensureDelegateType();
	}

	public DomainDeclaredType getBaseType() {
		return getBaseType(this);
	}
	
	private DomainDeclaredType getBaseType(DomainDeclared domain) {
		return this;
	}
}