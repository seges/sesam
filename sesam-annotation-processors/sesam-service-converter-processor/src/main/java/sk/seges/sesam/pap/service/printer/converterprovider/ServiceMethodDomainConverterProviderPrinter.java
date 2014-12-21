package sk.seges.sesam.pap.service.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.converterprovider.AbstractDomainMethodConverterProviderPrinter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.Field;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.service.printer.model.ServiceConverterProviderPrinterContext;

public class ServiceMethodDomainConverterProviderPrinter extends AbstractDomainMethodConverterProviderPrinter {

	public ServiceMethodDomainConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw, ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, pw, converterProviderPrinter);
	}

    @Override
	protected void printResult(AbstractProviderPrinterContext context) {
		pw.print("return (", getTypedDtoConverter(), ") ");
		
		MutableDeclaredType fieldType = processingEnv.getTypeUtils().getDeclaredType(processingEnv.getTypeUtils().toMutableType(Class.class), 
				processingEnv.getTypeUtils().getTypeVariable(ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX));

		Field field = new Field(DOMAIN_CLASS_PARAMETER_NAME, fieldType);
		field.setCastType(processingEnv.getTypeUtils().getDeclaredType(processingEnv.getTypeUtils().toMutableType(Class.class), new MutableDeclaredType[] { context.getRawDomain() }));

		converterProviderPrinter.printDomainGetConverterMethodName(context.getRawDomain(), field, 
				((ServiceConverterProviderPrinterContext)context).getLocalMethod(), pw, false);
	}
	
	@Override
	protected void printType(MutableTypeMirror type, AbstractProviderPrinterContext context) {

		DomainType domainType = processingEnv.getTransferObjectUtils().getDomainType(type);
		if (domainType.getKind().isDeclared() && domainType.getConverter() != null) {
			context = new ServiceConverterProviderPrinterContext((DomainDeclaredType)domainType, ((ServiceConverterProviderPrinterContext)context).getLocalMethod());
			print(context);
		}
	}
	
	@Override
	public void print(AbstractProviderPrinterContext context) {

		if (types.size() == 0) {
			initializeProviderMethod();
		}

		if (!types.contains(context.getRawDomain().getCanonicalName())) {
			types.add(context.getRawDomain().getCanonicalName());		
		}
	}
}