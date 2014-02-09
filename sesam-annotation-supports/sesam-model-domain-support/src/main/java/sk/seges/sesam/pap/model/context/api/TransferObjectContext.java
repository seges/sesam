package sk.seges.sesam.pap.model.context.api;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;

public interface TransferObjectContext {

	public static final String LOCAL_CONVERTER_NAME = "converter";

	boolean isSuperclassMethod();

	DomainType getDomainMethodReturnType();

	String getDomainFieldPath();

	String getDomainFieldName();

	DtoType getDtoFieldType();

	String getDtoFieldName();

	ConfigurationTypeElement getConfigurationTypeElement();

	Modifier getModifier();

	ExecutableElement getDtoMethod();

	ExecutableElement getDomainMethod();

	boolean isCustomerConstructorParameter();
	int getCustomerConstructorParameterIndex();

	/** Converter stuff */
	ConverterTypeElement getConverter();

	boolean useConverter();

	boolean hasKey();
}