package sk.seges.sesam.pap.model.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import sk.seges.sesam.core.pap.model.ConstructorParameter;
import sk.seges.sesam.core.pap.model.ConverterConstructorParameter;
import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.api.MutableWildcardType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableTypes;
import sk.seges.sesam.core.pap.structure.DefaultPackageValidatorProvider;
import sk.seges.sesam.core.pap.structure.api.PackageValidatorProvider;
import sk.seges.sesam.pap.model.model.api.GeneratedClass;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.printer.converter.ConverterInstancerType;
import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;
import sk.seges.sesam.pap.model.utils.ConstructorHelper;
import sk.seges.sesam.shared.model.converter.BasicCachedConverter;
import sk.seges.sesam.shared.model.converter.api.InstantiableDtoConverter;

public class ConverterTypeElement extends TomBaseDeclaredType implements GeneratedClass {

	public static final String DEFAULT_SUFFIX = "Converter";	
	public static final String DEFAULT_CONFIGURATION_SUFFIX = "Configuration";
	public static final String DTO_TYPE_ARGUMENT_PREFIX = "DTO";
	public static final String DOMAIN_TYPE_ARGUMENT_PREFIX = "DOMAIN";

	private final TypeElement converterTypeElement;
	private final ConfigurationTypeElement configurationTypeElement;
	
	private MutableDeclaredType converterBase;
	
	private final boolean generated;
	
	ConverterTypeElement(ConfigurationTypeElement configurationTypeElement, TypeElement converterTypeElement, EnvironmentContext<TransferObjectProcessingEnvironment> envContext) {
		super(envContext);
		this.converterTypeElement = converterTypeElement;
		this.configurationTypeElement = configurationTypeElement;
		this.generated = false;
		
		initialize();
	}
	
	public ConverterTypeElement(ConfigurationTypeElement configurationTypeElement, EnvironmentContext<TransferObjectProcessingEnvironment> envContext) {
		super(envContext);
		this.generated = true;
		this.converterTypeElement = null;
		this.configurationTypeElement = configurationTypeElement;

		setKind(MutableTypeKind.CLASS);
		setSuperClass(getTypeUtils().getDeclaredType(
				getTypeUtils().toMutableType(getGeneratedSuperClass()),
					configurationTypeElement.getDto()/*.stripTypeParametersTypes()*/,
					configurationTypeElement.getDomain()/*.stripTypeParametersTypes()*/));
	}

	protected Class<?> getGeneratedSuperClass() {
		return BasicCachedConverter.class;
	}

	public boolean isConverterInstantiable() {
		return getTypeUtils().implementsType(this, getTypeUtils().toMutableType(InstantiableDtoConverter.class));
	}

	private MutableTypeMirror stripWildcards(MutableTypeMirror converter) {
		if (converter.getKind().isDeclared()) {
			return stripWildcards((MutableDeclaredType)converter);
		}
		
		return converter;
	}
	
	private MutableDeclaredType stripWildcards(MutableDeclaredType converter) {

		List<? extends MutableTypeVariable> typeVariables = converter.getTypeVariables();
		
		MutableTypeVariable[] strippedTypeVariables = new MutableTypeVariable[typeVariables.size()];

		int i = 0;
		for (MutableTypeVariable typeVariable: typeVariables) {
			if (typeVariable.getKind().equals(MutableTypeKind.WILDCARD)) {
				MutableWildcardType wildcardType = (MutableWildcardType)typeVariable;
				if (wildcardType.getSuperBound() != null) {
					typeVariable = environmentContext.getProcessingEnv().getTypeUtils().getTypeVariable(null, stripWildcards(wildcardType.getSuperBound()));
				} else if (wildcardType.getExtendsBound() != null) {
					typeVariable = environmentContext.getProcessingEnv().getTypeUtils().getTypeVariable(null, stripWildcards(wildcardType.getExtendsBound()));
				} else {
					typeVariable = environmentContext.getProcessingEnv().getTypeUtils().getTypeVariable(null, 
							environmentContext.getProcessingEnv().getTypeUtils().toMutableType(Object.class));
				}
			} else if (typeVariable.getVariable() != null /*&& typeVariable.getVariable().equals(MutableWildcardType.WILDCARD_NAME)*/) {
				if (typeVariable.getUpperBounds().size() == 1) {
					typeVariable = environmentContext.getProcessingEnv().getTypeUtils().getTypeVariable(null, stripWildcards(typeVariable.getUpperBounds().iterator().next()));
				} else if (typeVariable.getLowerBounds().size() == 1) {
					typeVariable = environmentContext.getProcessingEnv().getTypeUtils().getTypeVariable(null, stripWildcards(typeVariable.getLowerBounds().iterator().next()));
				} else {
					typeVariable = environmentContext.getProcessingEnv().getTypeUtils().getTypeVariable(null, 
							environmentContext.getProcessingEnv().getTypeUtils().toMutableType(Object.class));
				}
			}
			strippedTypeVariables[i++] = typeVariable;
		}
		
		converter.setTypeVariables(strippedTypeVariables);
		
		return converter;
	}
	
	public MutableDeclaredType getConverterBase() {

		if (ensureDelegateType().hasTypeParameters() || !isConverterInstantiable()) {
			return stripWildcards(ensureDelegateType().clone());
		}
		
		if (converterBase != null) {
			return converterBase;
		}
		
		MutableTypes typeUtils = getTypeUtils();

		DomainType domain = getDomain();

		converterBase = typeUtils.getDeclaredType(typeUtils.toMutableType(InstantiableDtoConverter.class), 
				typeUtils.getTypeVariable(null, domain.getDto()), 
				typeUtils.getTypeVariable(null, domain));	
		
		return converterBase;
	}
	
	protected MutableDeclaredType getDelegate() {
		if (converterTypeElement != null) {
			return (MutableDeclaredType) getTypeUtils().toMutableType((DeclaredType)converterTypeElement.asType());
		}
		return getGeneratedConverterTypeFromConfiguration(configurationTypeElement);
	}

	private void initialize() {
		if (this.hasVariableParameterTypes() && configurationTypeElement.getDomain().hasTypeParameters()) {

			MutableTypeVariable[] converterParameters = new MutableTypeVariable[configurationTypeElement.getDomain().getTypeVariables().size() +
					configurationTypeElement.getDto().getTypeVariables().size()];
			
			int i = 0;
			
			for (MutableTypeVariable typeVariable: configurationTypeElement.getDto().getTypeVariables()) {
				converterParameters[i] = typeVariable;
				i++;
			}

			for (MutableTypeVariable typeVariable: configurationTypeElement.getDomain().getTypeVariables()) {
				converterParameters[i] = typeVariable;
				i++;
			}
			
			setDelegate(this.clone().setTypeVariables(converterParameters));
		}
	}
	
	protected PackageValidatorProvider getPackageValidationProvider() {
		return new DefaultPackageValidatorProvider();
	}

	private List<MutableTypeVariable> prefixTypeArguments(String prefix, DeclaredType declaredType, MutableDeclaredType referenceType) {
		
		List<MutableTypeVariable> result = new ArrayList<MutableTypeVariable>();
		
		Iterator<? extends MutableTypeVariable> iterator = referenceType.getTypeVariables().iterator();
		
		int i = 0;
		while (iterator.hasNext()) {
			if (i >= declaredType.getTypeArguments().size()) {
				break;
			}
			i++;
			MutableTypeVariable typeParameter = iterator.next();
			
			String name = typeParameter.getVariable();
			
			if (name != null && name.length() > 0) {
				result.add(typeParameter.clone().setVariable(prefix + "_" + name));
			}
		}
		
		return result;
	}

	private MutableDeclaredType getGeneratedConverterTypeFromConfiguration(ConfigurationTypeElement configurationTypeElement) {

		Element configurationElement = configurationTypeElement.asConfigurationElement();
		
		if (!configurationElement.asType().getKind().equals(TypeKind.DECLARED)) {
			return null;
		}
		
		DeclaredType declaredType = (DeclaredType)configurationTypeElement.asConfigurationElement().asType();
		
		TransferObjectMappingAccessor transferObjectConfiguration = new TransferObjectMappingAccessor((TypeElement)declaredType.asElement(), environmentContext.getProcessingEnv());
		
		TypeElement converter = transferObjectConfiguration.getConverter();
		if (converter != null || !configurationTypeElement.ensureDelegateType().getKind().isDeclared()) {
			return null;
		}

		TypeElement domainType = transferObjectConfiguration.getDomain();

		if (domainType == null) {
		    //there is no domain entityprovider defined, so converter does not make any sense
			return null;
		}

		//We are going to modify simple name, so clone is necessary
		MutableDeclaredType configurationNameType = ((MutableDeclaredType)configurationTypeElement.ensureDelegateType()).clone();
		
		//Remove configuration suffix if it is there - just to have better naming convention
		String simpleName = configurationNameType.getSimpleName();
		if (simpleName.endsWith( DEFAULT_CONFIGURATION_SUFFIX) && simpleName.length() > DEFAULT_CONFIGURATION_SUFFIX.length()) {
			simpleName = simpleName.substring(0, simpleName.lastIndexOf(DEFAULT_CONFIGURATION_SUFFIX));
			configurationNameType = configurationNameType.setSimpleName(simpleName);
		}
		
		MutableDeclaredType converterNameType = configurationNameType.addClassSufix(DEFAULT_SUFFIX);
		
		if (domainType.getTypeParameters().size() > 0) {
			
			MutableDeclaredType referenceType = getTypeUtils().toMutableType((DeclaredType)domainType.asType());
			
			//there are type parameters, so they should be passed into the converter definition itself
			List<MutableTypeVariable> typeParameters = prefixTypeArguments(DTO_TYPE_ARGUMENT_PREFIX, (DeclaredType)domainType.asType(), referenceType);
			typeParameters.addAll(prefixTypeArguments(DOMAIN_TYPE_ARGUMENT_PREFIX, (DeclaredType)domainType.asType(), referenceType));

			converterNameType.setTypeVariables(typeParameters.toArray(new MutableTypeVariable[] {}));
		}
		
		return converterNameType;
	}
	
	@Override
	public boolean isGenerated() {
		return generated;
	}
	
	public TypeElement asElement() {
		return converterTypeElement;
	}
	
	public ConfigurationTypeElement getConfiguration() {
		return configurationTypeElement;
	}

	private ConverterConstructorParameter toConverterParameter(ParameterElement parameter) {
		return new ConverterConstructorParameter(parameter, environmentContext.getProcessingEnv());
	}
	
	private ConverterConstructorParameter toConverterConstructorParameter(ConstructorParameter constructorParameter) {
		return new ConverterConstructorParameter(constructorParameter.getType(), 
				constructorParameter.getName(), null, PropagationType.PROPAGATED_IMUTABLE, environmentContext.getProcessingEnv());
	}
	
	private List<ExecutableElement> getSortedConstructorMethods(TypeElement element) {
		List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
		Collections.sort(constructors, new Comparator<ExecutableElement>() {

			@Override
			public int compare(ExecutableElement o1, ExecutableElement o2) {
				int size1 = ((ExecutableType)o1.asType()).getParameterTypes().size();
				int size2 = ((ExecutableType)o2.asType()).getParameterTypes().size();
				return size2 - size1;
			}
		});
		
		return constructors;
	}

	public List<ConverterConstructorParameter> getConverterParameters(ConverterConstructorParametersResolver parametersResolver) {
		return getConverterParameters(parametersResolver, ConverterInstancerType.REFERENCED_CONVERTER_INSTANCER);
	}
	
	public List<ConverterConstructorParameter> getConverterParameters(ConverterConstructorParametersResolver parametersResolver, ConverterInstancerType converterInstancerType) {

		List<ConverterConstructorParameter> parameters = new LinkedList<ConverterConstructorParameter>();

		TypeElement converterTypeElement = getElementUtils().getTypeElement(getCanonicalName());

		MutableTypes typeUtils = environmentContext.getProcessingEnv().getTypeUtils();

		if (converterTypeElement != null && !isGenerated()) {
			List<ExecutableElement> constructors = getSortedConstructorMethods(converterTypeElement);
			
			ParameterElement[] constructorAditionalParameters = new ParameterElement[0];
			
			if (getConfiguration() != null && getConfiguration().getDomain() != null) {
				constructorAditionalParameters = parametersResolver.getConstructorAditionalParameters();
			}
			
			if (constructors != null) {
				
				List<ConstructorParameter> constructorParameters = null;
				
				//TODO
				if (converterInstancerType.getConstructorIndex() != -1) {
					if (constructors.size() > converterInstancerType.getConstructorIndex()) {
						constructorParameters = ConstructorHelper.getConstructorParameters(getTypeUtils(), constructors.get(converterInstancerType.getConstructorIndex()));
					} else {
						constructorParameters = ConstructorHelper.getConstructorParameters(getTypeUtils(), constructors.get(0));
					}
				} else {
					constructorParameters = ConstructorHelper.getConstructorParameters(getTypeUtils(), constructors.get(constructors.size() - 1));
				}
				
				for (ConstructorParameter converterParameter : constructorParameters) {

					ConverterConstructorParameter param = toConverterConstructorParameter(converterParameter);

					for (ParameterElement constructorAditionalParameter: constructorAditionalParameters) {
						if (typeUtils.isAssignable(constructorAditionalParameter.getType(), typeUtils.toMutableType(converterParameter.getType()))) {
							param = new ConverterConstructorParameter(constructorAditionalParameter.getType(),
									param.getName(), null, param.getPropagationType(), environmentContext.getProcessingEnv());
						}

						if (constructorAditionalParameter.getPropagationType().equals(PropagationType.INSTANTIATED) && typeUtils.isSameType(constructorAditionalParameter.getType(), typeUtils.toMutableType(converterParameter.getType()))) {
							param.setPropagationType(constructorAditionalParameter.getPropagationType());
							break;
						}
					}

					parameters.add(param);
				}
			}
		} else {
			TypeElement cachedConverterType = getElementUtils().getTypeElement(BasicCachedConverter.class.getCanonicalName());
			List<ExecutableElement> constructors = getSortedConstructorMethods(cachedConverterType);

			ParameterElement[] constructorAditionalParameters = parametersResolver.getConstructorAditionalParameters();

			if (constructors != null && constructors.size() > converterInstancerType.getConstructorIndex()) {

				List<? extends VariableElement> constructorParameters = null;
				if (converterInstancerType.getConstructorIndex() != -1) {
					constructorParameters = constructors.get(converterInstancerType.getConstructorIndex()).getParameters();
				} else {
					constructorParameters = constructors.get(constructors.size() - 1).getParameters();
				}

				for (VariableElement constructorParameter : constructorParameters) {
					boolean isAdditional = false;
					
					for (ParameterElement constructorAditionalParameter: constructorAditionalParameters) {
						if (typeUtils.isSameType(typeUtils.toMutableType(constructorParameter.asType()), constructorAditionalParameter.getType())) {
							isAdditional = true;
							break;
						}
					}

					if (!isAdditional) {
						parameters.add(toConverterConstructorParameter(ConstructorHelper.toConverterParameter(getTypeUtils(), constructorParameter)));
					}
				}
			}

			for (ParameterElement constructorAditionalParameter: constructorAditionalParameters) {
				parameters.add(toConverterParameter(constructorAditionalParameter));
			}
		}

		//Parameter name resolving. Renaming arg0 to entityManager, etc.
		DomainType domain = getDomain();
		ParameterElement[] constructorAditionalParameters = null;
		
		if (domain != null && domain.getKind().isDeclared()) {
			TypeMirror domainType = ((DomainDeclaredType)domain).asType();
			if (domainType != null) {
				constructorAditionalParameters = parametersResolver.getConstructorAditionalParameters();
			}
		}
		
		for (ConverterConstructorParameter converterParameter: parameters) {
			if (/*asElement() == null && */constructorAditionalParameters != null) {
				for (ParameterElement additionalParameter: constructorAditionalParameters) {
					if (getTypeUtils().isSameType(additionalParameter.getType(), converterParameter.getType())) {
						converterParameter.merge(additionalParameter);
					}
				}
			}
		}
		
		return parameters;
	}
	
	public DtoType getDto() {
		DomainType domain = getDomain();
		
		if (domain == null) {
			return null;
		}
		
		return domain.getDto();
	}
	
	public DomainType getInstantiableDomain() {
		return getDomain().getDomainDefinitionConfiguration().getInstantiableDomain();
	}
	
	public DomainType getDomain() {
		if (getConfiguration() == null) {
			return null;
		}
		
		return getConfiguration().getDomain();
	}
}