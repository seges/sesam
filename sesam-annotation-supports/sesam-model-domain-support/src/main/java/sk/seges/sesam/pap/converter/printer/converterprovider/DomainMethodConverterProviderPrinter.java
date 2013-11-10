package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.utils.MutableTypes;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.model.ConverterProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.shared.model.converter.api.DtoConverter;

public class DomainMethodConverterProviderPrinter extends AbstractDomainMethodConverterProviderPrinter {

	public DomainMethodConverterProviderPrinter(ConverterConstructorParametersResolverProvider parametersResolverProvider, 
			TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw, ConverterProviderPrinter converterProviderPrinter) {
		super(parametersResolverProvider, processingEnv, pw, converterProviderPrinter);
	}

	@Override
	protected void printResultConverter(ConverterProviderPrinterContext context) {
		//MutableTypes typeUtils = processingEnv.getTypeUtils();
		//pw.print("return (", getTypedDtoConverter(), ") (", typeUtils.getDeclaredType(typeUtils.toMutableType(DtoConverter.class), context.getDto(), context.getDomain()), ")");
		pw.print("return (", getTypedDtoConverter(), ")");
		converterProviderPrinter.printDomainGetConverterMethodName(context.getRawDomain(), null, null, pw, false);
	}
}