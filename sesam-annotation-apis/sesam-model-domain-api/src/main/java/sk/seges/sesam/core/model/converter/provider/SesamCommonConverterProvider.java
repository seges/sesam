package sk.seges.sesam.core.model.converter.provider;

import java.util.Map;

import sk.seges.sesam.pap.model.annotation.ConverterProviderDefinition;
import sk.seges.sesam.security.server.model.acl.AclDataRegistry;
import sk.seges.sesam.security.server.model.acl.AclSecuredEntity;
import sk.seges.sesam.security.shared.model.api.ClientSecuredEntity;
import sk.seges.sesam.server.model.converter.common.MapConverter;
import sk.seges.sesam.server.model.converter.security.SecuredEntityConverter;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;
import sk.seges.sesam.shared.model.converter.api.DtoConverter;
import sk.seges.sesam.shared.model.converter.provider.AbstractConverterProvider;

@ConverterProviderDefinition
public class SesamCommonConverterProvider extends AbstractConverterProvider {

	private final ConverterProviderContext converterProviderContext;
	private final AclDataRegistry aclDataRegistry;
	
	public SesamCommonConverterProvider(ConverterProviderContext converterProviderContext, AclDataRegistry aclDataRegistry) {
		this.converterProviderContext = converterProviderContext;
		this.aclDataRegistry = aclDataRegistry;
	}

	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDomain(Class<DOMAIN> domainClass) {

		if (domainClass == null) {
			return null;
		}

		if (Map.class.isAssignableFrom(domainClass)) {
			return getMapConverter();
		}

		if (AclSecuredEntity.class.equals(domainClass)) {
			return getSecuredEntityConverter();
		}
		
		return null;
	}

	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDto(Class<DTO> dtoClass) {

		if (dtoClass == null) {
			return null;
		}

		if (Map.class.isAssignableFrom(dtoClass)) {
			return getMapConverter();
		}

		if (ClientSecuredEntity.class.equals(dtoClass)) {
			return getSecuredEntityConverter();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	protected <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getSecuredEntityConverter() {
		SecuredEntityConverter securedEntityConverter = new SecuredEntityConverter(converterProviderContext, aclDataRegistry);
		securedEntityConverter.setCache(cache);
		return (DtoConverter<DTO, DOMAIN>)securedEntityConverter;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getMapConverter() {
		return new MapConverter(converterProviderContext);
	}
}