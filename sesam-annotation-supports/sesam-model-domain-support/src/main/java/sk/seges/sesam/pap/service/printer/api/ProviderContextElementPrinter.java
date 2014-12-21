package sk.seges.sesam.pap.service.printer.api;

import sk.seges.sesam.pap.converter.model.AbstractProviderContextType;
import sk.seges.sesam.pap.converter.model.AbstractProviderType;

public interface ProviderContextElementPrinter {

	void initialize(AbstractProviderContextType contextType);
	
	void print(AbstractProviderType serviceConverterProvider);
	
	void finish(AbstractProviderContextType contextType);

}
