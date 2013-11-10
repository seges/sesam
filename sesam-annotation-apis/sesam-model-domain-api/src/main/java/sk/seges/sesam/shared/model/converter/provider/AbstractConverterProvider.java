package sk.seges.sesam.shared.model.converter.provider;

import sk.seges.sesam.shared.model.converter.ConvertedInstanceCache;
import sk.seges.sesam.shared.model.converter.api.ConverterProvider;
import sk.seges.sesam.shared.model.converter.api.DtoConverter;

public abstract class AbstractConverterProvider implements ConverterProvider {

	protected ConvertedInstanceCache cache;

	public void setCache(ConvertedInstanceCache cache) {
		this.cache = cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDomain(DOMAIN domain) {
		if (domain == null) {
			return null;
		}
		return (DtoConverter<DTO, DOMAIN>) getConverterForDomain(domain.getClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDto(DTO dto) {
		if (dto == null) {
			return null;
		}
		return (DtoConverter<DTO, DOMAIN>) getConverterForDto(dto.getClass());
	}
}