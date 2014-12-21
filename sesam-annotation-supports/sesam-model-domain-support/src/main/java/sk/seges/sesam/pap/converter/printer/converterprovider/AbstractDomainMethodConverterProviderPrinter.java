package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.converter.printer.model.ConverterProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;

public abstract class AbstractDomainMethodConverterProviderPrinter extends AbstractConverterProviderPrinter {

	protected AbstractDomainMethodConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw, ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, pw, converterProviderPrinter);
	}

	protected static final String DOMAIN_CLASS_PARAMETER_NAME = "domainClass";

    @Override
	public void initializeProviderMethod() {
		pw.println("public <" + ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX + ", " + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX +
				"> ", getTypedDtoConverter(), " " + ConverterTargetType.DOMAIN.getConverterMethodName() + "(", Class.class.getSimpleName(), "<" + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX + "> ",
				DOMAIN_CLASS_PARAMETER_NAME + ") {");
		pw.println();
		pw.println("if (" + DOMAIN_CLASS_PARAMETER_NAME + " == null) {");
		pw.println("return null;");
		pw.println("}");
		pw.println();
	}

    @Override
    protected boolean checkContext(AbstractProviderPrinterContext context) {
        return context.getDomain().getKind().isDeclared() && context.getConverterType() != null;
    }

    @Override
    protected MutableDeclaredType getTargetEntity(AbstractProviderPrinterContext context) {
        return context.getConverterType().getConfiguration().getInstantiableDomain();
    }

    @Override
    protected String getParameterName() {
        return DOMAIN_CLASS_PARAMETER_NAME;
    }

	@Override
	protected void printType(MutableTypeMirror type, AbstractProviderPrinterContext context) {
		DomainType domainType = processingEnv.getTransferObjectUtils().getDomainType(type);
		context = new ConverterProviderPrinterContext((DomainDeclaredType)domainType);
		print(context);
	}
}