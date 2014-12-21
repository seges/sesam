package sk.seges.sesam.shared.model.provider.api;

import java.util.List;

/**
 * Created by PeterSimun on 8.12.2014.
 */
public interface EntityMetaInfoProvider {

    <DTO, DOMAIN> List<Class<?>> getDtoClassForDomain(Class<DOMAIN> domainClass);

    //<DTO, DOMAIN> Class<DOMAIN> getDomainClassForDto(Class<DTO> dtoClass);
}
