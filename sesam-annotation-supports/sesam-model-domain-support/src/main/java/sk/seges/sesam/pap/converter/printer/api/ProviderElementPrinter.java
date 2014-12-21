package sk.seges.sesam.pap.converter.printer.api;

import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;

public interface ProviderElementPrinter {

	void initialize();
	
	void print(AbstractProviderPrinterContext context);
	
	void finish();
}