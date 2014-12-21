package sk.seges.sesam.pap.converter.printer;

import sk.seges.sesam.pap.converter.printer.api.ProviderElementPrinter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;

public class ConverterVerifier implements ProviderElementPrinter {

	private boolean containsConverters = false;
	
	@Override
	public void initialize() {
	}

	@Override
	public void print(AbstractProviderPrinterContext context) {
		containsConverters = true;
	}

	public boolean isContainsConverter() {
		return containsConverters;
	}

	@Override
	public void finish() {
	}
}