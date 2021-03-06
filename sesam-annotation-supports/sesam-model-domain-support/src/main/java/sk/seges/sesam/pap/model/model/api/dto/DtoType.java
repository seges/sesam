package sk.seges.sesam.pap.model.model.api.dto;

import java.util.List;

import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.api.GeneratedClass;
import sk.seges.sesam.pap.model.model.api.HasConverter;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;

public interface DtoType extends MutableTypeMirror, GeneratedClass, HasConverter {

	List<ConfigurationTypeElement> getConfigurations();
	ConfigurationTypeElement getDomainDefinitionConfiguration();

	DomainType getDomain();
}