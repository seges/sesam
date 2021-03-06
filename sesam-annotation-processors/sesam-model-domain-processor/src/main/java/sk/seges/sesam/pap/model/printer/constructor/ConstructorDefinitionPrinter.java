package sk.seges.sesam.pap.model.printer.constructor;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.printer.AbstractElementPrinter;
import sk.seges.sesam.pap.model.printer.api.TransferObjectElementPrinter;

public class ConstructorDefinitionPrinter extends AbstractElementPrinter implements TransferObjectElementPrinter {

	private int index = 0;
	
	public ConstructorDefinitionPrinter(FormattedPrintWriter pw) {
		super(pw);
	}

	@Override
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputName) {
		pw.print("public " + outputName.getSimpleName() + "(");
	}

	@Override
	public void finish(ConfigurationTypeElement configurationTypeElement) {
		pw.println(") {");
	}

	@Override
	public void print(TransferObjectContext context) {
		if (context.isSuperclassMethod()) {
			return;
		}

		if (index > 0) {
			pw.print(", ");
		}
		pw.print(context.getDtoFieldType(), " " + context.getDtoFieldName());
		index++;
	}
}