package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;

public class DomainMethodConverterProviderPrinter extends AbstractDomainMethodConverterProviderPrinter {

	public DomainMethodConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw, ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, pw, converterProviderPrinter);
	}

	@Override
	protected void printResult(AbstractProviderPrinterContext context) {
		pw.print("return (", getTypedDtoConverter(), ")");
		converterProviderPrinter.printDomainGetConverterMethodName(context.getRawDomain(), null, null, pw, false);
	}
}