package sk.seges.sesam.pap.model.model;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sk.seges.sesam.core.pap.NullCheck;
import sk.seges.sesam.core.pap.accessor.AnnotationAccessor;
import sk.seges.sesam.core.pap.model.InitializableValue;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.utils.AnnotationClassPropertyHarvester;
import sk.seges.sesam.core.pap.utils.AnnotationClassPropertyHarvester.AnnotationClassProperty;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.domain.ValueHolder;
import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;
import sk.seges.sesam.pap.model.annotation.TransferObjectMapping.NotDefinedConverter;
import sk.seges.sesam.pap.model.annotation.TransferObjectMappings;
import sk.seges.sesam.shared.model.converter.api.DtoConverter;

/**
 * Helper class that provides configuration parameters for the {@link TypeElement}. Type has to have
 * {@link TransferObjectMapping} or {@link TransferObjectMappings} annotation in order to read the configuration. If the
 * type does not holds any of these annotations it is considered as non configuration element and helper class would no
 * provide relevant results
 * 
 * @author Peter Simun (simun@seges.sk)
 */
public class TransferObjectMappingAccessor extends AnnotationAccessor {

	enum DtoParameterType {
		DTO(0), DOMAIN(1);

		private int index;

		private DtoParameterType(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
	}
	
	private Set<TransferObjectMapping> mappings = new HashSet<TransferObjectMapping>();

	private TransferObjectMapping referenceMapping;
	private Element configurationHolderElement;

	private final InitializableValue<TypeElement> domainValue = new InitializableValue<TypeElement>();
	private final InitializableValue<Boolean> converterGeneratedValue = new InitializableValue<Boolean>();
	private final InitializableValue<Boolean> dtoGeneratedValue = new InitializableValue<Boolean>();
	private final InitializableValue<TypeElement> dtoInterfaceValue = new InitializableValue<TypeElement>();
	private final InitializableValue<TypeElement> domainInterface = new InitializableValue<TypeElement>();
	private final InitializableValue<TypeElement> dtoValue = new InitializableValue<TypeElement>();
	private final InitializableValue<TypeElement> converterValue = new InitializableValue<TypeElement>();

	/**
	 * TypeElement holds {@link TransferObjectMapping} or {@link TransferObjectMappings} annotation
	 */
	public TransferObjectMappingAccessor(Element element, MutableProcessingEnvironment processingEnv) {
		super(processingEnv);

		this.configurationHolderElement = element;

		if (element.getAnnotationMirrors().size() > 0) {
			{
				TransferObjectMapping mapping = element.getAnnotation(TransferObjectMapping.class);
				if (mapping != null) {
					this.mappings.add(mapping);
				}
			}

			if (this.mappings.size() == 0) {
				TransferObjectMappings mappings = element.getAnnotation(TransferObjectMappings.class);
				if (mappings != null) {
					for (TransferObjectMapping mapping : mappings.value()) {
						this.mappings.add(mapping);
					}
				}
			}

			if (mappings.size() == 1) {
				referenceMapping = mappings.iterator().next();
			}
		}
	}

	public boolean isValid() {
		return mappings.size() > 0;
	}
	
	TransferObjectMapping getReferenceMapping() {
		return referenceMapping;
	}
	
	Element getConfigurationHolderElement() {
		return configurationHolderElement;
	}
	
	void setReferenceMapping(TransferObjectMapping referenceMapping) {
		this.referenceMapping = referenceMapping;
	}
	
	public TypeElement getEvaluatedDomainType() {
		if (referenceMapping == null) {
			return null;
		}

		return getEvaluatedDomainType(referenceMapping);
	}
	
	public TransferObjectMapping getMappingForDto(MutableDeclaredType dtoType) {
		for (TransferObjectMapping mapping : mappings) {
			TypeElement annotationDtoType = getDto(mapping);

			if (annotationDtoType != null) {
				if (dtoType.getQualifiedName().equals(annotationDtoType.getQualifiedName().toString())) {
					return mapping;
				}
			}

			annotationDtoType = getDtoInterface(mapping);

			if (annotationDtoType != null) {
				MutableDeclaredType mutableAnnotationDtoType = processingEnv.getTypeUtils().toMutableType((DeclaredType)annotationDtoType.asType());
				if (processingEnv.getTypeUtils().implementsType(dtoType, mutableAnnotationDtoType)) {
					return mapping;
				}
			}
		}
		
		return null;
	}

	public TransferObjectMapping getMappingForDomain(MutableDeclaredType domainType) {
		for (TransferObjectMapping mapping : mappings) {
			TypeElement annotationDomainType = getDomain(mapping);
			MutableDeclaredType mutableAnnotationDomainType = null;
			
			if (annotationDomainType != null) {
				mutableAnnotationDomainType = processingEnv.getTypeUtils().toMutableType((DeclaredType)annotationDomainType.asType());
				if (processingEnv.getTypeUtils().isSameType(mutableAnnotationDomainType, domainType)) {
					return mapping;
				}
			}

			annotationDomainType = getDomainInterface(mapping);
			if (annotationDomainType != null) {
				mutableAnnotationDomainType = processingEnv.getTypeUtils().toMutableType((DeclaredType)annotationDomainType.asType());
				if (processingEnv.getTypeUtils().implementsType(domainType, mutableAnnotationDomainType)) {
					return mapping;
				}
			}
		}
		
		return null;
	}
	
	public TransferObjectMapping getMappingForDomain(DeclaredType domainType) {
		for (TransferObjectMapping mapping : mappings) {
			TypeElement annotationDomainType = getDomain(mapping);

			if (annotationDomainType != null) {
				if (processingEnv.getTypeUtils().erasure(annotationDomainType.asType()).equals(
					processingEnv.getTypeUtils().erasure(domainType))) {
					return mapping;
				}
			}

			annotationDomainType = getDomainInterface(mapping);

			if (annotationDomainType != null && ProcessorUtils.implementsType(domainType, annotationDomainType.asType())) {
				return mapping;
			}
		}
		
		return null;
	}

	protected TypeElement getEvaluatedDomainType(TransferObjectMapping mapping) {

		// getting the domain definition
		TypeElement domain = NullCheck.checkNull(getDomain(mapping));
		if (domain != null) {
			return domain;
		}

		// getting the converter definition
		TypeElement converter = getConverter(mapping);

		if (converter != null) {
			TypeElement domainElement = getDomainClassFromConverter(converter);

			if (domainElement != null) {
				return domainElement;
			} 
		}

		// getting the configuration definition
		TypeElement configuration = getConfiguration(mapping);
		if (configuration != null) {
			
			TypeElement domainType = new TransferObjectMappingAccessor(configuration, processingEnv).getEvaluatedDomainType();

			if (domainType != null) {
				return domainType;
			}
		}

		return null;
	}

	/**
	 * Returns domain object defined in the {@link TransferObjectMapping} annotation. If there are more the one
	 * {@link TransferObjectMapping} annotations null type is returned. If there is any DTO class specified in the
	 * {@link TransferObjectMapping} method will return null result. Method is used to get domain class only in the
	 * configurations that has no DTO specified.
	 */
	public TypeElement getDomain() {
		if (domainValue.isInitialized()) {
			return domainValue.getValue();
		}

		if (referenceMapping == null) {
			return domainValue.setValue(null);
		}

		return domainValue.setValue(getDomain(referenceMapping));
	}

	public TypeElement getDtoInterface() {

		if (dtoInterfaceValue.isInitialized()) {
			return dtoInterfaceValue.getValue();
		}

		if (referenceMapping == null) {
			return dtoInterfaceValue.setValue(null);
		}

		return dtoInterfaceValue.setValue(getDtoInterface(referenceMapping));
	}
	
	public TypeElement getDomainInterface() {
		if (referenceMapping == null) {
			return null;
		}

		return getDomainInterface(referenceMapping);
	}

	public TypeElement getConfiguration() {
		if (referenceMapping == null) {
			return null;
		}

		return getConfiguration(referenceMapping);
	}

	public boolean isConverterGenerated() {

		if (converterGeneratedValue.isInitialized()) {
			return converterGeneratedValue.getValue();
		}

		if (referenceMapping == null) {
			return converterGeneratedValue.setValue(false);
		}
		
		return converterGeneratedValue.setValue(isConverterGenerated(referenceMapping));
	}

	public boolean isDtoGenerated() {
		if (dtoGeneratedValue.isInitialized()) {
			return dtoGeneratedValue.getValue();
		}

		if (referenceMapping == null) {
			return dtoGeneratedValue.setValue(false);
		}
		
		return dtoGeneratedValue.setValue(isDtoGenerated(referenceMapping));
	}

	boolean isDtoGenerated(TransferObjectMapping mapping) {
		return mapping.generateDto();
	}

	boolean isConverterGenerated(TransferObjectMapping mapping) {
		return mapping.generateConverter();
	}

	public TypeElement getConverter() {

		if (converterValue.isInitialized()) {
			return converterValue.getValue();
		}

		if (referenceMapping == null) {
			return converterValue.setValue(null);
		}

		return converterValue.setValue(getConverter(referenceMapping));
	}

	public TypeElement getDto() {
		if (dtoValue.isInitialized()) {
			return dtoValue.getValue();
		}

		if (referenceMapping == null) {
			return dtoValue.setValue(null);
		}

		return dtoValue.setValue(getDto(referenceMapping));
	}

	TypeElement getDomain(TransferObjectMapping mapping) {
		TypeElement domainType = NullCheck.checkNull(AnnotationClassPropertyHarvester.getTypeOfClassProperty(mapping,
				new AnnotationClassProperty<TransferObjectMapping>() {

					@Override
					public Class<?> getClassProperty(TransferObjectMapping annotation) {
						return annotation.domainClass();
					}
				}, processingEnv));
		
		if (domainType != null) {
			return domainType;
		}
		
		String domainClassName = NullCheck.checkNull(mapping.domainClassName());
		
		if (domainClassName != null) {
			return processingEnv.getElementUtils().getTypeElement(domainClassName);
		}
		
		TypeElement delegatedConfiguration = getConfiguration();
		
		if (delegatedConfiguration != null) {
			return new TransferObjectMappingAccessor(delegatedConfiguration, processingEnv).getDomain();
		}
		
		return null;
	}

	TypeElement getDomainInterface(TransferObjectMapping mapping) {
		if (!domainInterface.isInitialized()) {
			TypeElement domainInterfaceType = NullCheck.checkNull(AnnotationClassPropertyHarvester.getTypeOfClassProperty(mapping,
					new AnnotationClassProperty<TransferObjectMapping>() {

						@Override
						public Class<?> getClassProperty(TransferObjectMapping annotation) {
							return annotation.domainInterface();
						}
					}));

			if (domainInterfaceType != null) {
				domainInterface.setValue(domainInterfaceType);
				return domainInterface.getValue();
			}

			String domainInterfaceName = NullCheck.checkNull(mapping.domainInterfaceName());

			if (domainInterfaceName != null) {
				domainInterface.setValue(processingEnv.getElementUtils().getTypeElement(domainInterfaceName));
				return domainInterface.getValue();
			}

			TypeElement delegatedConfiguration = getConfiguration();

			if (delegatedConfiguration != null) {
				domainInterface.setValue(new TransferObjectMappingAccessor(delegatedConfiguration, processingEnv).getDomainInterface(mapping));
				return domainInterface.getValue();
			}

			domainInterface.setValue(null);
		}

		return domainInterface.getValue();
	}

	TypeElement getDtoInterface(TransferObjectMapping mapping) {
		TypeElement dtoInterfaceType = NullCheck.checkNull(AnnotationClassPropertyHarvester.getTypeOfClassProperty(mapping,
				new AnnotationClassProperty<TransferObjectMapping>() {

					@Override
					public Class<?> getClassProperty(TransferObjectMapping annotation) {
						return annotation.dtoInterface();
					}
				}));
		
		if (dtoInterfaceType != null) {
			return dtoInterfaceType;
		}
		
		String dtoInterfaceName = NullCheck.checkNull(mapping.dtoInterfaceName());
		
		if (dtoInterfaceName != null) {
			return processingEnv.getElementUtils().getTypeElement(dtoInterfaceName);
		}
		
		return null;
	}

	TypeElement getDto(TransferObjectMapping mapping) {
		TypeElement dtoElement = NullCheck.checkNull(AnnotationClassPropertyHarvester.getTypeOfClassProperty(mapping,
				new AnnotationClassProperty<TransferObjectMapping>() {

					@Override
					public Class<?> getClassProperty(TransferObjectMapping annotation) {
						return annotation.dtoClass();
					}
				}));
		
		if (dtoElement != null) {
			return dtoElement;
		}
		
		String dtoClassName = NullCheck.checkNull(mapping.dtoClassName());
		
		if (dtoClassName != null) {
			return processingEnv.getElementUtils().getTypeElement(dtoClassName);
		}
		
		TypeElement delegatedConfiguration = getConfiguration();
		
		if (delegatedConfiguration != null) {
			return new TransferObjectMappingAccessor(delegatedConfiguration, processingEnv).getDto();
		}

		return null;
	}

	TypeElement getConverter(TransferObjectMapping mapping) {
		TypeElement converter = NullCheck.checkNull(AnnotationClassPropertyHarvester.getTypeOfClassProperty(mapping,
				new AnnotationClassProperty<TransferObjectMapping>() {

					@Override
					public Class<?> getClassProperty(TransferObjectMapping annotation) {
						return annotation.converter();
					}
				}), NotDefinedConverter.class);
		
		if (converter != null) {
			return converter;
		}
		
		String converterClassName = NullCheck.checkNull(mapping.converterClassName());
		
		if (converterClassName != null) {
			return processingEnv.getElementUtils().getTypeElement(converterClassName);
		}
		
		TypeElement delegatedConfiguration = getConfiguration();
		
		if (delegatedConfiguration != null) {
			return new TransferObjectMappingAccessor(delegatedConfiguration, processingEnv).getConverter();
		}
		
		return null;
	}

	TypeElement getConfiguration(TransferObjectMapping mapping) {
		TypeElement configurationClass = NullCheck.checkNull(AnnotationClassPropertyHarvester.getTypeOfClassProperty(mapping,
				new AnnotationClassProperty<TransferObjectMapping>() {

					@Override
					public Class<?> getClassProperty(TransferObjectMapping annotation) {
						return annotation.configuration();
					}
				}));
		
		if (configurationClass != null) {
			return configurationClass;
		}
		
		String configurationClassName = NullCheck.checkNull(mapping.configurationClassName());
		
		if (configurationClassName != null) {
			return processingEnv.getElementUtils().getTypeElement(configurationClassName);
		}
		
		return null;
	}

	protected TypeElement getDomainClassFromConverter(TypeElement converterType) {
		return erasure(converterType, DtoConverter.class, DtoParameterType.DOMAIN.getIndex());
	}

	protected TypeElement erasure(TypeElement rootElement, Class<?> parameterOwnerClass, int parameterIndex) {
		TypeElement domainTypeElement = null;

		TypeElement parameterHolderType = processingEnv.getElementUtils().getTypeElement(
				parameterOwnerClass.getCanonicalName());

		while (domainTypeElement == null) {

			TypeElement owner = null;
			TypeElement holderElement = null;

			if (parameterHolderType.getKind().equals(ElementKind.INTERFACE)) {
				owner = getTypeThatImplements(rootElement, parameterHolderType);
			} else if (parameterHolderType.getKind().equals(ElementKind.CLASS)) {
				owner = getTypeThatExtends(rootElement, parameterHolderType);
			}

			if (owner == null) {
				return null;
			}

			if (parameterHolderType.getKind().equals(ElementKind.INTERFACE)) {
				holderElement = getImplementedInterface(owner, parameterHolderType);
			} else if (parameterHolderType.getKind().equals(ElementKind.CLASS)) {
				holderElement = (TypeElement) ((DeclaredType) owner.getSuperclass()).asElement();
			}

			TypeParameterElement typeParameterElement = holderElement.getTypeParameters().get(parameterIndex);

			if (typeParameterElement.asType().getKind().equals(TypeKind.DECLARED)) {
				return (TypeElement) ((DeclaredType) typeParameterElement.asType()).asElement();
			} else if (typeParameterElement.asType().getKind().equals(TypeKind.TYPEVAR)) {
				parameterHolderType = owner;
				parameterIndex = getPrameterIndex(owner, ((TypeVariable) typeParameterElement.asType()).asElement()
						.getSimpleName().toString());
			}
		}

		return null;
	}

	protected int getPrameterIndex(TypeElement owner, String name) {
		int index = 0;
		for (TypeParameterElement ownerParameter : owner.getTypeParameters()) {
			if (ownerParameter.getSimpleName().toString().equals(name)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	protected TypeElement getImplementedInterface(TypeElement typeElement, TypeElement interfaceElement) {
		for (TypeMirror implementedInterfaceType : typeElement.getInterfaces()) {
			if (implementedInterfaceType.getKind().equals(TypeKind.DECLARED)) {
				DeclaredType interfaceDeclaredType = (DeclaredType) implementedInterfaceType;
				if (interfaceDeclaredType.asElement().getSimpleName().toString()
						.equals(interfaceElement.getSimpleName().toString())) {
					if (processingEnv.getElementUtils().getPackageOf(interfaceDeclaredType.asElement()).getQualifiedName().toString().equals(
						processingEnv.getElementUtils().getPackageOf(interfaceElement).getQualifiedName().toString())) {
						return (TypeElement) interfaceDeclaredType.asElement();
					}
				}
			}
		}

		return null;
	}

	protected TypeElement getTypeThatExtends(TypeElement rootElement, TypeElement type) {
		TypeMirror superclass = rootElement.getSuperclass();
		if (superclass != null && superclass.getKind().equals(TypeKind.DECLARED)) {

			TypeElement superclassType = (TypeElement) ((DeclaredType) superclass).asElement();

			if (type.getQualifiedName().toString().equals(superclassType.getQualifiedName().toString())) {
				return rootElement;
			}

			return getTypeThatExtends(superclassType, type);
		}
		return null;
	}

	protected TypeElement getTypeThatImplements(TypeElement rootElement, TypeElement interfaceElement) {
		for (TypeMirror implementedInterfaceType : rootElement.getInterfaces()) {
			if (implementedInterfaceType.getKind().equals(TypeKind.DECLARED)) {
				DeclaredType interfaceDeclaredType = (DeclaredType) implementedInterfaceType;
				if (interfaceDeclaredType.asElement().getSimpleName().toString()
						.equals(interfaceElement.getSimpleName().toString())) {
					if (processingEnv.getElementUtils().getPackageOf(interfaceDeclaredType.asElement()).getQualifiedName().toString().equals(
						processingEnv.getElementUtils().getPackageOf(interfaceElement).getQualifiedName().toString())) {
						return rootElement;
					}
				}
				TypeElement typeThatImplements = getTypeThatImplements((TypeElement) interfaceDeclaredType.asElement(),
						interfaceElement);
				if (typeThatImplements != null) {
					return typeThatImplements;
				}
			}
		}

		TypeMirror superclass = rootElement.getSuperclass();

		if (superclass != null && superclass.getKind().equals(TypeKind.DECLARED)) {
			TypeElement typeThatImplements = getTypeThatImplements((TypeElement) ((DeclaredType) superclass).asElement(), interfaceElement);

			if (typeThatImplements != null) {
				return typeThatImplements;
			}
		}

		return null;
	}
}