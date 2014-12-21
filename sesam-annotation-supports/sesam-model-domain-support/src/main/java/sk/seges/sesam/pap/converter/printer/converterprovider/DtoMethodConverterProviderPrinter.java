package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.converter.printer.model.ConverterProviderPrinterContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;

public class DtoMethodConverterProviderPrinter extends AbstractDtoMethodConverterProviderPrinter {

	public DtoMethodConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw, ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, pw, converterProviderPrinter);
	}

	@Override
	protected void printResult(AbstractProviderPrinterContext context) {
		pw.print("return (", getTypedDtoConverter(), ") ");
		if (context.getRawDto().getConverter() != null) {
			converterProviderPrinter.printDtoGetConverterMethodName(context.getRawDto(), null, null, pw, false);
		} else {
			converterProviderPrinter.printDtoGetConverterMethodName(context.getDomain().getDto(), null, null, pw, false);
		}
	}

	@Override
	protected void printType(MutableTypeMirror type, AbstractProviderPrinterContext context) {

		DtoType dtoType = processingEnv.getTransferObjectUtils().getDtoType(type);
		if (dtoType.getKind().isDeclared() && dtoType.getConverter() != null) {
			print(new ConverterProviderPrinterContext((DtoDeclaredType)dtoType, context.getConfigurationType()));
		}
	}
}