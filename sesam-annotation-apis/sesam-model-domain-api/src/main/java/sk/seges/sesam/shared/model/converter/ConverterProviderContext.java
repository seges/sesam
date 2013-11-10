package sk.seges.sesam.shared.model.converter;

import java.util.ArrayList;
import java.util.List;

import sk.seges.sesam.shared.model.converter.api.ConverterProvider;
import sk.seges.sesam.shared.model.converter.api.DtoConverter;

public abstract class ConverterProviderContext {

	protected List<ConverterProvider> availableConverterProviders = new ArrayList<ConverterProvider>();
	protected ConvertedInstanceCache cache;

	public void registerConverterProvider(ConverterProvider converterProvider) {
		availableConverterProviders.add(converterProvider);
	}

	public void setCache(ConvertedInstanceCache cache) {
		this.cache = cache;
	}

	public abstract ConverterProviderContext get();

	protected <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> initializeConverter(DtoConverter<DTO, DOMAIN> converter) {
		if (converter instanceof BasicCachedConverter) {
			((BasicCachedConverter)converter).setCache(cache);
		}

		return converter;
	}

	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDomain(DOMAIN domain) {
		if (domain == null) {
			return null;
		}
		
		for (ConverterProvider availableConverterProvider: availableConverterProviders) {
			DtoConverter<DTO, DOMAIN> result = availableConverterProvider.getConverterForDomain(domain);
			
			if (result != null) {
				return initializeConverter(result);
			}
		}

		return null;
	}

	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDomain(Class<DOMAIN> domainClass) {
		for (ConverterProvider availableConverterProvider: availableConverterProviders) {
			DtoConverter<DTO, DOMAIN> result = availableConverterProvider.getConverterForDomain(domainClass);
			
			if (result != null) {
				return initializeConverter(result);
			}
		}

		return null;
	}

	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDto(DTO dto) {
		if (dto == null) {
			return null;
		}
		
		for (ConverterProvider availableConverterProvider: availableConverterProviders) {
			DtoConverter<DTO, DOMAIN> result = availableConverterProvider.getConverterForDto(dto);
			
			if (result != null) {
				return initializeConverter(result);
			}
		}

		return null;
	}

	public <DTO, DOMAIN> DtoConverter<DTO, DOMAIN> getConverterForDto(Class<DTO> dtoClass) {
		for (ConverterProvider availableConverterProvider: availableConverterProviders) {
			DtoConverter<DTO, DOMAIN> result = availableConverterProvider.getConverterForDto(dtoClass);
			
			if (result != null) {
				return initializeConverter(result);
			}
		}

		return null;
	}
}