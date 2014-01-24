package sk.seges.sesam.pap.model.context;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableTypes;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.pap.model.accessor.KeyAccessor;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.ConvertAccessor;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.EnvironmentContext;
import sk.seges.sesam.pap.model.model.TransferObjectMappingAccessor;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.TransferObjectTypes;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;
import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

public class TransferObjectProcessorContext implements TransferObjectContext {
		
	protected final ConfigurationTypeElement configurationTypeElement;

	protected final Modifier modifier;
	protected final ExecutableElement method;
	protected final ExecutableElement domainMethod;

	protected DomainType domainMethodReturnType;

	protected DtoType fieldType;
	protected String fieldName;

	protected String domainFieldName;
	protected String domainFieldPath;

	protected ConverterTypeElement converterType;
	protected boolean useConverter;

	private boolean superClassMethod;
	private boolean hasKey;
	
	public TransferObjectProcessorContext(ConfigurationTypeElement configurationTypeElement, Modifier modifier, ExecutableElement method, boolean superClassMethod) {
		this(configurationTypeElement, modifier, method, method, superClassMethod);
	}

	public TransferObjectProcessorContext(ConfigurationTypeElement configurationTypeElement, Modifier modifier, ExecutableElement method,
			ExecutableElement domainMethod, boolean superClassMethod) {
		
		this.configurationTypeElement = configurationTypeElement;
		this.modifier = modifier;
		this.method = method;
		this.superClassMethod = superClassMethod;
		this.domainMethod = domainMethod;
	}

	@Override
	public boolean isSuperclassMethod() {
		return superClassMethod;
	}

	@Override
	public boolean hasKey() {
		return hasKey;
	}

	protected TypeMirror erasure(TypeElement typeElement, TypeMirror param) {

		if (typeElement == null) {
			return param;
		}
		
		if (!param.getKind().equals(TypeKind.TYPEVAR)) {
			return param;
		}
		
		TypeVariable typeVar = (TypeVariable)param;
		//TODO support also typevariables and wildcards?
		if (typeVar.getUpperBound().getKind().equals(TypeKind.DECLARED) && !typeVar.getUpperBound().toString().equals(Object.class.getName())) {
			DtoType convertedTypeVar = getTransferObjectUtils().getDomainType(typeVar.getUpperBound()).getDto();
			if (convertedTypeVar != null) {
				return typeVar.getUpperBound();
			}
		}
		
		//TODO support also typevariables and wildcards?
		if (typeVar.getLowerBound().getKind().equals(TypeKind.DECLARED) && !typeVar.getUpperBound().toString().equals(Object.class.getName())) {
			DtoType convertedTypeVar = getTransferObjectUtils().getDomainType(typeVar.getLowerBound()).getDto();
			if (convertedTypeVar != null) {
				return typeVar.getLowerBound();
			}
		}

		return ProcessorUtils.erasure(typeElement, typeVar);
	}

	protected EnvironmentContext<TransferObjectProcessingEnvironment> envContext;
	
	protected TransferObjectTypes getTransferObjectUtils() {
		return envContext.getProcessingEnv().getTransferObjectUtils();
	}
	
	protected MutableTypes getTypeUtils() {
		return envContext.getProcessingEnv().getTypeUtils();
	}
	
	protected Messager getMessager() {
		return envContext.getProcessingEnv().getMessager();
	}
	
	public boolean initialize(EnvironmentContext<TransferObjectProcessingEnvironment> envContext, EntityResolver entityResolver) {
		return initialize(envContext, entityResolver, null);
	}
	
	public boolean initialize(EnvironmentContext<TransferObjectProcessingEnvironment> envContext, EntityResolver entityResolver, String path) {

		this.envContext = envContext;
		
		if (getDtoMethod() == null) {
			getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find DTO method for property " + path, configurationTypeElement.asConfigurationElement());
			return false;
		}

		hasKey = new KeyAccessor(method, envContext.getProcessingEnv()).isValid();

		if (path == null) {
			this.domainFieldPath = TransferObjectHelper.getFieldPath(getDtoMethod());
			this.fieldName = MethodHelper.toField(getDtoMethod());
		} else {
			this.domainFieldPath = path + "." + MethodHelper.toField(getDtoMethod());
			this.fieldName = MethodHelper.toField(getDomainFieldPath());
		}

		this.domainFieldName = MethodHelper.toGetter(getDomainFieldPath());

		DomainDeclaredType domainTypeElement = configurationTypeElement.getDomain();
		
		DtoType type = null;
		
		if (entityResolver.getTargetEntityType(getDtoMethod()).getKind().equals(TypeKind.TYPEVAR)) {
			TypeMirror returnType = erasure(domainTypeElement.asConfigurationElement(), entityResolver.getTargetEntityType(getDtoMethod()));
			if (returnType != null) {
				type = getTransferObjectUtils().getDomainType(returnType).getDto();
				if (type == null) {
					type = getTransferObjectUtils().getDtoType(returnType);
				}
			} else {
				type = handleDomainTypeParameter(entityResolver);
				
				if (type == null) {
					return false;
				}
			}
		} else {
			TypeMirror targetEntityType = entityResolver.getTargetEntityType(getDtoMethod());
			
			type = getTransferObjectUtils().getDomainType(targetEntityType).getDto();
			if (type == null) {
				type = getTransferObjectUtils().getDtoType(targetEntityType);
			}
		}

		TypeMirror targetReturnType = null;
		
		if (getDomainMethod() != null) {
			targetReturnType = entityResolver.getTargetEntityType(getDomainMethod());
		} else {
			//in this case there is no domain method and field is inherited from interface
			targetReturnType = getDtoMethod().getReturnType();
		}
		
		DomainType domainReturnType = getTransferObjectUtils().getDomainType(targetReturnType);
		
		if (targetReturnType.getKind().equals(TypeKind.TYPEVAR)) {
			//check whether type variable is locally defined only for method or is
			//type variable for whole class
			
			String variableName = ((TypeVariable)targetReturnType).asElement().getSimpleName().toString();
			
			List<? extends TypeParameterElement> typeParameters = domainTypeElement.asConfigurationElement().getTypeParameters();
			
			boolean classTypeVariable = false;
			
			if (typeParameters != null) {
				for (TypeParameterElement typeParameter: typeParameters) {
					if (typeParameter.getSimpleName().toString().equals(variableName)) {
						classTypeVariable = true;
						break;
					}
				}
			}
			
			if (!classTypeVariable) {
				TypeMirror erasedType = erasure(domainTypeElement.asConfigurationElement(), targetReturnType);
				if (erasedType != null && !erasedType.toString().equals(Object.class.getCanonicalName())) {
					domainReturnType = getTransferObjectUtils().getDomainType(erasedType);
				}
			}
			/* else {
				TypeMirror targetEntityType = getTargetEntityType(context.getDomainMethod());
				if (targetEntityType != null && !context.getDomainMethod().getReturnType().equals(targetEntityType)) {
					domainReturnType = targetEntityType;
				}
			}*/
		}

		if (getDtoMethod().getReturnType().getKind().equals(TypeKind.VOID)) {
			
			type = getTransferObjectUtils().getDomainType(domainReturnType).getDto();

			if (type == null) {
				getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find DTO alternative for " + entityResolver.getTargetEntityType(getDomainMethod()).toString() + ". Skipping getter " + 
						getDtoMethod().getSimpleName().toString(),configurationTypeElement.asConfigurationElement());
				return false;
			}
		}

		this.fieldType = type;

		setDomainMethodReturnType(domainReturnType);

		if (getDtoMethod().getReturnType().equals(TypeKind.VOID) && domainReturnType.getKind().equals(TypeKind.DECLARED)) {
			
			MutableTypeMirror domainReturnMutableType = getTypeUtils().toMutableType(domainReturnType);
			
			if (!getTypeUtils().isSameType(type, domainReturnMutableType)) {
				
				DtoType dtoType = null;
				
				if (domainReturnType.getKind().equals(TypeKind.DECLARED)) {
					dtoType = getTransferObjectUtils().getDomainType(domainReturnType).getDto();
				}
	
				if (dtoType == null || !getTransferObjectUtils().isSameType(dtoType, type)) {
					getMessager().printMessage(Kind.ERROR, "[ERROR] Return type from domain method " + domainReturnType.toString() + " " + domainTypeElement.getCanonicalName() + 
							"." + getDomainFieldName() + " is not compatible with specified return type in the DTO " + type.toString() + ". Please, check your configuration " + 
							configurationTypeElement.getCanonicalName(), configurationTypeElement.asConfigurationElement());
					return false;
				}
			}
		} else if (getDtoMethod().getReturnType().equals(TypeKind.VOID)) {
			if (!getTypeUtils().isSameType(type, getTypeUtils().toMutableType(domainReturnType))) {
				getMessager().printMessage(Kind.ERROR, "[ERROR] Return type from domain method " + domainReturnType.toString() + " " + domainTypeElement.getCanonicalName() + 
						"." + getDomainFieldName() + " is not compatible with specified return type in the DTO " + type.toString() + ". Please, check your configuration " + 
						configurationTypeElement.getCanonicalName(), configurationTypeElement.asConfigurationElement());
				return false;
			}
		}

		intializeConverter();
		
		return true;
	}
	
	protected void intializeConverter() {

		DomainType returnType = getDomainMethodReturnType();
//		if (returnType.getDomainDefinitionConfiguration() != null) {
//			returnType = returnType.getDomainDefinitionConfiguration().getInstantiableDomain();
//		}

		TransferObjectMappingAccessor transferObjectMappingAccessor = new TransferObjectMappingAccessor(getDtoMethod(), envContext.getProcessingEnv());
		if (transferObjectMappingAccessor.isValid()) {

				ConfigurationContext context = new ConfigurationContext(envContext.getConfigurationEnv());
				ConfigurationTypeElement configurationType = null;

				switch (returnType.getKind()) {
				case TYPEVAR:
					//configurationType = new ConfigurationTypeElement(transferObjectMappingAccessor.getConverter(), envContext, context);
					TypeElement converter = transferObjectMappingAccessor.getConverter();
					TypeMirror superclass = converter.getSuperclass();
					
					if (superclass != null && superclass.getKind().equals(TypeKind.DECLARED) && ((DeclaredType)superclass).getTypeArguments().size() == 2) {
						TypeMirror dto = ((DeclaredType)superclass).getTypeArguments().get(0);
						TypeMirror domain = ((DeclaredType)superclass).getTypeArguments().get(1);
						
						if (dto.getKind().equals(TypeKind.TYPEVAR)) {
							//TODO
							throw new RuntimeException("TODO - Implement me!");
						}

						if (domain.getKind().equals(TypeKind.TYPEVAR)) {
							//TODO
							throw new RuntimeException("TODO - Implement me!");
						}
						
						configurationType = new ConfigurationTypeElement((MutableDeclaredType)envContext.getProcessingEnv().getTypeUtils().toMutableType(domain), 
								(MutableDeclaredType)envContext.getProcessingEnv().getTypeUtils().toMutableType(dto), getDtoMethod(), envContext, context);
					} else {
						//TODO add support for 
						throw new RuntimeException("TODO - Implement me!");
					}
					break;
				case INTERFACE:
				case CLASS:
					configurationType = new ConfigurationTypeElement(getDtoMethod(), (DomainDeclaredType) returnType, envContext, context);
					break;
				default:
					configurationType = new ConfigurationTypeElement(getDtoMethod(), envContext, context);
					break;
				}

				List<ConfigurationTypeElement> configurations = new ArrayList<ConfigurationTypeElement>();
				configurations.add(configurationType);
				context.setConfigurations(configurations);
				
				this.converterType = configurationType.getConverter();
				
				if (this.converterType != null) {
					return;
				}
		}

		switch (returnType.getKind()) {
		case TYPEVAR:
			//we don't know exact type, so try to convert it using converter provider
			ConvertAccessor convertAccessor = new ConvertAccessor(getDtoMethod(), envContext.getProcessingEnv());
			useConverter = !convertAccessor.isValid() || convertAccessor.getValue();
			break;
		case ARRAY:
			//TODO
			break;
		case INTERFACE:
		case CLASS:

			ConfigurationContext context = new ConfigurationContext(envContext.getConfigurationEnv());
			ConfigurationTypeElement configurationType = envContext.getConfigurationEnv().getConfiguration(getDtoMethod(), ((DomainDeclaredType) returnType).getBaseType(), context);
			List<ConfigurationTypeElement> configurations = new ArrayList<ConfigurationTypeElement>();
			configurations.add(configurationType);
			context.setConfigurations(configurations);
			
			//reads TransferObjectConfiguration annotation from method in the configuration
			ConverterTypeElement converterTypeElement = configurationType.getConverter();

			if (converterTypeElement != null) {
				this.converterType = converterTypeElement;
			} else {
				//
				if (returnType.getDomainDefinitionConfiguration() != null) {
					this.converterType = returnType.getDomainDefinitionConfiguration().getDomain().getConverter();
				} else {
					this.converterType = null;
				}
//				this.converterType = returnType.getConverter();
			}
			
			break;
		default:
			break;
		}
	}

	protected DtoType handleDomainTypeParameter(EntityResolver entityResolver) {
		envContext.getProcessingEnv().getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find erasure for the " + 
				entityResolver.getTargetEntityType(getDomainMethod()).toString() + " in the method: " + getDtoFieldName(), 
				getConfigurationTypeElement().asConfigurationElement());
		return null;
	}

	public void setDomainMethodReturnType(DomainType domainMethodReturnType) {
		this.domainMethodReturnType = domainMethodReturnType;
	}

	public DomainType getDomainMethodReturnType() {
		return domainMethodReturnType;
	}

	public String getDomainFieldPath() {
		return domainFieldPath;
	}

	public String getDomainFieldName() {
		return domainFieldName;
	}

	public void setFieldType(DtoType fieldType) {
		this.fieldType = fieldType;
	}
	
	public DtoType getDtoFieldType() {
		return fieldType;
	}

	public String getDtoFieldName() {
		return fieldName;
	}

	public ConfigurationTypeElement getConfigurationTypeElement() {
		return configurationTypeElement;
	}

	public Modifier getModifier() {
		return modifier;
	}

	public ExecutableElement getDtoMethod() {
		return method;
	}

	public ExecutableElement getDomainMethod() {
		return domainMethod;
	}
	
	@Override
	public ConverterTypeElement getConverter() {
		return converterType;
	}

	@Override
	public boolean useConverter() {
		return useConverter;
	}
}