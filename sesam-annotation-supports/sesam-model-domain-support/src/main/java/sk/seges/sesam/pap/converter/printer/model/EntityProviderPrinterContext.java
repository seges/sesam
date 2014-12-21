package sk.seges.sesam.pap.converter.printer.model;

import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;

/**
 * Created by PeterSimun on 14.12.2014.
 */
public class EntityProviderPrinterContext extends AbstractProviderPrinterContext {

    public EntityProviderPrinterContext(DtoDeclaredType dtoType, ConfigurationTypeElement configurationType) {
        super(dtoType, configurationType);
    }

    public EntityProviderPrinterContext(DomainDeclaredType domainType) {
        super(domainType);
    }

    protected EntityProviderPrinterContext(DomainDeclaredType rawDomain, DomainDeclaredType domain, DtoDeclaredType rawDto, DtoDeclaredType dto, ConverterTypeElement converterType) {
        super(rawDomain, domain, rawDto, dto, converterType);
    }
}
