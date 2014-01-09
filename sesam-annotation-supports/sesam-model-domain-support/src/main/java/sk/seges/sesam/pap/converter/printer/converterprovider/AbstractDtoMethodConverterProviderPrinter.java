package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.AbstractObjectConverterProviderPrinter;
import sk.seges.sesam.pap.converter.printer.model.ConverterProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;

public abstract class AbstractDtoMethodConverterProviderPrinter extends AbstractObjectConverterProviderPrinter {

	protected static final String DTO_CLASS_PARAMETER_NAME = "dto";

	public AbstractDtoMethodConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw,
			ConverterProviderPrinter converterProviderPrinter, ConverterConstructorParametersResolverProvider parametersResoverProvider) {
		super(processingEnv, pw, converterProviderPrinter, parametersResoverProvider);
	}

	public void initializeDtoConverterMethod() {
		pw.println("public <"	+ ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX + ", " + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX +
				"> ", getTypedDtoConverter(), " " + ConverterTargetType.DTO.getConverterMethodName() + "(", Class.class.getSimpleName(), "<" + ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX + "> " + DTO_CLASS_PARAMETER_NAME + ") {");
		pw.println();
		pw.println("if (" + DTO_CLASS_PARAMETER_NAME + " == null) {");
		pw.println("return null;");
		pw.println("}");
		pw.println();
	}
	
	@Override
	public void print(ConverterProviderPrinterContext context) {
		
		//There is no direct converter between DTO <-> DOMAIN, but DOMAIN <-> DTO neither
		if (context.getConverterType() == null && context.getDomain().getConverter() == null) {
			return;
		}
		
		if (!types.contains(context.getRawDto().getCanonicalName())) {
			
			if (types.size() == 0) {
				initializeDtoConverterMethod();
			}
			
			types.add(context.getRawDto().getCanonicalName());

			pw.print("if (", context.getRawDto().clone().setTypeVariables(new MutableTypeVariable[] {}), ".class.");
			pw.print(getClassAssignmentOperator(context.getConverterType()));
			pw.println("(" + DTO_CLASS_PARAMETER_NAME + ")) {");

			printResultConverter(context);
			
			pw.println(";");
			pw.println("}");
			pw.println();
		}

		printTypeVariables(context);
	}

	protected abstract void printResultConverter(ConverterProviderPrinterContext context);
}
