package sk.seges.sesam.pap.model.printer;

import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.AbstractConverterPrinter;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;

public class AbstractDtoPrinter extends AbstractConverterPrinter {

	protected AbstractDtoPrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider, TransferObjectProcessingEnvironment processingEnv) {
		super(parametersResolverProvider, processingEnv);
	}	
}