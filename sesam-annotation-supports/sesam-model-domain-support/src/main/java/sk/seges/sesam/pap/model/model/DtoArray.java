package sk.seges.sesam.pap.model.model;

import java.util.List;

import sk.seges.sesam.core.pap.model.mutable.api.MutableArrayType;
import sk.seges.sesam.core.pap.model.mutable.delegate.DelegateMutableArray;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.GeneratedClass;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoArrayType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;

public class DtoArray extends DelegateMutableArray implements GeneratedClass, DtoArrayType {

	private final DtoType dtoType;
	private final MutableProcessingEnvironment processingEnv;
	
	public DtoArray(DtoType dtoType, MutableProcessingEnvironment processingEnv) {
		this.dtoType = dtoType;
		this.processingEnv = processingEnv;
	}

	@Override
	public List<ConfigurationTypeElement> getConfigurations() {
		return dtoType.getConfigurations();
	}

	@Override
	public ConverterTypeElement getConverter() {
		return dtoType.getConverter();
	}

	@Override
	public DomainType getDomain() {
		return new DomainArray(dtoType.getDomain(), processingEnv);
	}

	@Override
	public boolean isGenerated() {
		return dtoType.isGenerated();
	}

	@Override
	protected MutableArrayType getDelegate() {
		return processingEnv.getTypeUtils().getArrayType(dtoType);
	}

	@Override
	public ConfigurationTypeElement getDomainDefinitionConfiguration() {
		return dtoType.getDomainDefinitionConfiguration();
	}

}
