/**
   Copyright 2011 Seges s.r.o.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package sk.seges.sesam.shared.model.converter;

import sk.seges.sesam.shared.model.converter.api.InstantiableDtoConverter;

import java.io.Serializable;

public abstract class BasicCachedConverter<DTO, DOMAIN> implements InstantiableDtoConverter<DTO, DOMAIN> {

	protected ConvertedInstanceCache cache;
	
	protected BasicCachedConverter() {}

	public void setCache(ConvertedInstanceCache cache) {
		this.cache = cache;
	}

	protected DTO putDtoIntoCache(Object domainSource, DTO dtoResult, Serializable dtoId) {
		if (dtoResult != null) {
			if (dtoId != null) {
				cache.putDtoInstance(dtoResult, domainSource, dtoId);
			}
			cache.putDtoInstance(dtoResult, domainSource);
		}
		
		return dtoResult;
	}
	
	@SuppressWarnings("unchecked")
	protected DTO getDtoFromCache(Object domainSource, Serializable domainId) {
		DTO result = cache.getDtoInstance(domainSource);

		if (result != null) {
			return result;
		}
		
		if (domainId != null && domainSource != null) {
			result = (DTO) cache.getDtoInstance(domainSource.getClass(), domainId);
		}
		
		return result;
	}
	
	protected DOMAIN putDomainIntoCache(Object dtoSource, DOMAIN domainResult, Serializable domainId) {
		if (domainResult != null) {
			if (domainId != null) {
				cache.putDomainInstance(domainResult, dtoSource, domainId);
			}
			cache.putDomainInstance(domainResult, dtoSource);
		}
		
		return domainResult;
	}

	@SuppressWarnings("unchecked")
	protected DOMAIN getDomainFromCache(Object dtoSource, Serializable dtoId) {
		DOMAIN result = cache.getDomainInstance(dtoSource);

		if (result != null) {
			return result;
		}
		
		if (dtoId != null && dtoSource != null) {
			result = (DOMAIN) cache.getDomainInstance(dtoSource.getClass(), dtoId);
		}
		
		return result;
	}
	
	/**
	 * Loads DTO instance from the cache
	 */
	@Override
	public DTO getDtoInstance(Object domainSource, Serializable domainId) {
		return getDtoFromCache(domainSource, domainId);
	}
	
	/**
	 * Loads DOMAIN instance from the cache
	 */
	@Override
	public DOMAIN getDomainInstance(Object dtoSource, Serializable dtoId) {
		return getDomainFromCache(dtoSource, dtoId);
	}	
}