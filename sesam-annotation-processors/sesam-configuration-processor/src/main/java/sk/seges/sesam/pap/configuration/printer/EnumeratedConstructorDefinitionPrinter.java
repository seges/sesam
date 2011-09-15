package sk.seges.sesam.pap.configuration.printer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import sk.seges.sesam.core.pap.model.api.NamedType;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.configuration.model.SettingsContext;
import sk.seges.sesam.pap.configuration.printer.api.SettingsElementPrinter;

public class EnumeratedConstructorDefinitionPrinter extends AbstractSettingsElementPrinter implements SettingsElementPrinter {

	private FormattedPrintWriter pw;
	private int index = 0;
	
	public EnumeratedConstructorDefinitionPrinter(FormattedPrintWriter pw, ProcessingEnvironment processingEnv) {
		super(processingEnv);
		this.pw = pw;
	}

	@Override
	public void initialize(TypeElement type, NamedType outputName) {
		pw.print("public " + outputName.getSimpleName() + "(");
	}

	@Override
	public void print(SettingsContext context) {
		if (index > 0) {
			pw.print(", ");
		}

		if (context.getNestedElement() != null) {
			pw.print(context.getNestedOutputName().getSimpleName() + " " + context.getMethod().getSimpleName().toString());
		} else {
			pw.print(unboxType(context.getMethod().getReturnType()), " " + context.getMethod().getSimpleName().toString());
		}
		
		index++;
	}

	@Override
	public void finish(TypeElement type) {
		pw.println(") {");
	}

}
