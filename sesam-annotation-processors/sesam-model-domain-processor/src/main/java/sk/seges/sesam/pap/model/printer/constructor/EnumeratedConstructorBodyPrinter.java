package sk.seges.sesam.pap.model.printer.constructor;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;

public class EnumeratedConstructorBodyPrinter extends ConstructorBodyPrinter {

	private final ConstructorParameterHelper constructorParameterHelper;

	private boolean customConstructorDefined = false;

	public EnumeratedConstructorBodyPrinter(MutableProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(pw);
		this.constructorParameterHelper = new ConstructorParameterHelper(processingEnv);
	}

	@Override
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputName) {
		customConstructorDefined = constructorParameterHelper.hasCustomerConstructorDefined(configurationTypeElement);
	}

	@Override
	public void print(TransferObjectContext context) {
		//TODO handle parameter indexes also
		if (customConstructorDefined && context.isCustomerConstructorParameter()) {
			super.print(context);
		}
	}

	@Override
	public void finish(ConfigurationTypeElement configurationTypeElement) {
		if (customConstructorDefined) {
			super.finish(configurationTypeElement);
		}
	}
}