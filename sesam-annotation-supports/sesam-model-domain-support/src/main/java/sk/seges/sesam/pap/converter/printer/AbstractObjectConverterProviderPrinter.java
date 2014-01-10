package sk.seges.sesam.pap.converter.printer;

import java.util.HashSet;
import java.util.Set;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableTypes;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.api.ConverterProviderElementPrinter;
import sk.seges.sesam.pap.converter.printer.model.ConverterProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.AbstractConverterPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.shared.model.converter.api.DtoConverter;

public abstract class AbstractObjectConverterProviderPrinter extends AbstractConverterPrinter implements ConverterProviderElementPrinter {

	protected final FormattedPrintWriter pw;
	protected final ConverterProviderPrinter converterProviderPrinter;
	
	protected Set<String> types = new HashSet<String>();

	public AbstractObjectConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw,
			ConverterProviderPrinter converterProviderPrinter, ConverterConstructorParametersResolverProvider parametersResoverProvider) {
		super(parametersResoverProvider, processingEnv);
		this.pw = pw;
		this.converterProviderPrinter = converterProviderPrinter;
	}

	@Override
	public void initialize() {
		this.types.clear();
	}

	protected String getClassAssignmentOperator(ConverterTypeElement converter) {
		if (converter.getConfiguration().hasDtoInterfaceSpecified()) {
			return "isAssignableFrom";
		}

		return "equals";
	}

	protected MutableDeclaredType getTypedDtoConverter() {
		MutableTypes typeUtils = processingEnv.getTypeUtils();
		return typeUtils.getDeclaredType(typeUtils.toMutableType(DtoConverter.class), 
				typeUtils.getTypeVariable(ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX), 
				typeUtils.getTypeVariable(ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX));
	}

	@Override
	public void finish() {
		if (types.size() > 0) {
			pw.println("return null;");
			pw.println("}");
			pw.println();
		}
	}
	
	protected void printTypeVariables(ConverterProviderPrinterContext context) {
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

	protected abstract void printType(MutableTypeMirror type, ConverterProviderPrinterContext context);

}