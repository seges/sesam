package sk.seges.sesam.pap.model.printer.constructor;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class EnumeratedConstructorDefinitionPrinter extends ConstructorDefinitionPrinter {

	private final ConstructorParameterHelper constructorParameterHelper;

	private boolean customConstructorDefined = false;
	private List<TransferObjectContext> contexts = new ArrayList<TransferObjectContext>();

	public EnumeratedConstructorDefinitionPrinter(MutableProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(pw);
		this.constructorParameterHelper = new ConstructorParameterHelper(processingEnv);
	}

	@Override
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputName) {

		customConstructorDefined = constructorParameterHelper.hasCustomerConstructorDefined(configurationTypeElement);
		contexts.clear();

		if (customConstructorDefined) {
			super.initialize(configurationTypeElement, outputName);
		}
	}

	@Override
	public void print(TransferObjectContext context) {
		//TODO handle parameter indexes also
		if (customConstructorDefined && context.isCustomerConstructorParameter()) {
			contexts.add(context);
		}
	}

	@Override
	public void finish(ConfigurationTypeElement configurationTypeElement) {
		if (customConstructorDefined) {

			Collections.sort(contexts, new Comparator<TransferObjectContext>() {
				@Override
				public int compare(TransferObjectContext o1, TransferObjectContext o2) {
					if (o1.getCustomerConstructorParameterIndex() == -1 && o2.getCustomerConstructorParameterIndex() == -1) {
						//Sort if by their names
						return o1.getDtoFieldName().compareTo(o2.getDtoFieldName());
					}

					return new Integer(o1.getCustomerConstructorParameterIndex()).compareTo(o2.getCustomerConstructorParameterIndex());
				}
			});

			for (TransferObjectContext context: contexts) {
				super.print(context);
			}

			super.finish(configurationTypeElement);
		}
	}
}