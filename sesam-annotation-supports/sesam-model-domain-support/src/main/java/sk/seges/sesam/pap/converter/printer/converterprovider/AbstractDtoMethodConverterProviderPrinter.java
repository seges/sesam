package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;

public abstract class AbstractDtoMethodConverterProviderPrinter extends AbstractConverterProviderPrinter {

	protected static final String DTO_CLASS_PARAMETER_NAME = "dto";

	public AbstractDtoMethodConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw,
			ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, pw, converterProviderPrinter);
	}

	public void initializeProviderMethod() {
		pw.println("public <"	+ ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX + ", " + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX +
				"> ", getTypedDtoConverter(), " " + ConverterTargetType.DTO.getConverterMethodName() + "(", Class.class.getSimpleName(), "<" + ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX + "> " + DTO_CLASS_PARAMETER_NAME + ") {");
		pw.println();
		pw.println("if (" + DTO_CLASS_PARAMETER_NAME + " == null) {");
		pw.println("return null;");
		pw.println("}");
		pw.println();
	}

    protected boolean checkContext(AbstractProviderPrinterContext context) {
        //There is no direct converter between DTO <-> DOMAIN, but DOMAIN <-> DTO neither
        return context.getConverterType() != null || context.getDomain().getConverter() != null;
    }

    protected MutableDeclaredType getTargetEntity(AbstractProviderPrinterContext context) {
        return context.getConfigurationType().getDto();
    }

    protected String getParameterName() {
        return DTO_CLASS_PARAMETER_NAME;
    }
}