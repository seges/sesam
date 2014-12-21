package sk.seges.sesam.pap.converter.printer.model;

import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;

public class ConverterProviderPrinterContext extends AbstractProviderPrinterContext {

    public ConverterProviderPrinterContext(DtoDeclaredType dtoType, ConfigurationTypeElement configurationType) {
        super(dtoType, configurationType);
    }

    public ConverterProviderPrinterContext(DomainDeclaredType domainType) {
        super(domainType);
    }

    protected ConverterProviderPrinterContext(DomainDeclaredType rawDomain, DomainDeclaredType domain, DtoDeclaredType rawDto, DtoDeclaredType dto, ConverterTypeElement converterType) {
        super(rawDomain, domain, rawDto, dto, converterType);
    }
}