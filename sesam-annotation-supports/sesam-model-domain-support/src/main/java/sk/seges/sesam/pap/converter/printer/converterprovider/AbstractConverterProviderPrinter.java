package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableTypes;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.api.ProviderElementPrinter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.base.ProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.shared.model.converter.api.DtoConverter;

import java.util.Set;

public abstract class AbstractConverterProviderPrinter extends ProviderPrinter implements ProviderElementPrinter {

	protected final ConverterProviderPrinter converterProviderPrinter;

	public AbstractConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv,
                                            FormattedPrintWriter pw, ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, pw);
		this.converterProviderPrinter = converterProviderPrinter;
	}

	protected MutableDeclaredType getTypedDtoConverter() {
		MutableTypes typeUtils = processingEnv.getTypeUtils();
		return typeUtils.getDeclaredType(typeUtils.toMutableType(DtoConverter.class),
				typeUtils.getTypeVariable(ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX),
				typeUtils.getTypeVariable(ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX));
	}

    @Override
    public void print(AbstractProviderPrinterContext context) {
        super.print(context);

        //TODO is this doing something?
        printTypeVariables(context);
    }

    protected void printTypeVariables(AbstractProviderPrinterContext context) {
        for (MutableTypeVariable typeVariable: context.getRawDomain().getTypeVariables()) {

            Set<? extends MutableTypeMirror> lowerBounds = typeVariable.getLowerBounds();

            for (MutableTypeMirror lowerBound: lowerBounds) {
                printType(lowerBound, context);
            }

            Set<? extends MutableTypeMirror> upperBounds = typeVariable.getUpperBounds();

            for (MutableTypeMirror upperBound: upperBounds) {
                printType(upperBound, context);
            }
        }
    }

    protected abstract void printType(MutableTypeMirror type, AbstractProviderPrinterContext context);

}