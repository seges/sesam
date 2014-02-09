package sk.seges.sesam.pap.model.model.api.domain;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;

public interface DomainDeclaredType extends DomainType, MutableDeclaredType {

	TypeElement asConfigurationElement();

	DtoDeclaredType getDto();

	DomainDeclaredType getSuperClass();
	DomainDeclaredType getBaseType();

	DomainType getId(EntityResolver entityResolver);
	DomainType getDomainReference(EntityResolver entityResolver, String fieldName);

	ExecutableElement getIdMethod(EntityResolver entityResolver);
	ExecutableElement getMethodByName(String name);
	ExecutableElement getGetterMethod(String fieldName);
	//ExecutableElement getIsGetterMethod(String fieldName);
	ExecutableElement getSetterMethod(String fieldName);

	MutableDeclaredType asMutable();
}
