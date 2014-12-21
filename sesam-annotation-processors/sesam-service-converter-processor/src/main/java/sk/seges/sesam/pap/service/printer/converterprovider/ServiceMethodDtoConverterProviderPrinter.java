package sk.seges.sesam.pap.service.printer.converterprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.converterprovider.AbstractDtoMethodConverterProviderPrinter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.converter.printer.model.ConverterProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.Field;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.service.printer.model.ServiceConverterProviderPrinterContext;

public class ServiceMethodDtoConverterProviderPrinter extends AbstractDtoMethodConverterProviderPrinter {

	public ServiceMethodDtoConverterProviderPrinter(TransferObjectProcessingEnvironment processingEnv,
			FormattedPrintWriter pw, ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, pw, converterProviderPrinter);
	}

	@Override
	protected void printResult(AbstractProviderPrinterContext context) {
		pw.print("return (", getTypedDtoConverter(), ") ");
		MutableDeclaredType fieldType = processingEnv.getTypeUtils().getDeclaredType(processingEnv.getTypeUtils().toMutableType(Class.class), 
				processingEnv.getTypeUtils().getTypeVariable(ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX));
		Field field = new Field(DTO_CLASS_PARAMETER_NAME, fieldType);
		field.setCastType(processingEnv.getTypeUtils().getDeclaredType(processingEnv.getTypeUtils().toMutableType(Class.class), new MutableDeclaredType[] { context.getDto() }));
		converterProviderPrinter.printDtoGetConverterMethodName(context.getRawDto(), field, ((ServiceConverterProviderPrinterContext)context).getLocalMethod(), pw, false);
	}
	
	@Override
	protected void printType(MutableTypeMirror type, AbstractProviderPrinterContext context) {
		
		DtoType dtoType = processingEnv.getTransferObjectUtils().getDtoType(type);
		if (dtoType.getKind().isDeclared() && dtoType.getConverter() != null) {
			print(new ServiceConverterProviderPrinterContext((DtoDeclaredType)dtoType, ((ServiceConverterProviderPrinterContext)context).getLocalMethod(), 
					context.getConfigurationType()));
		}
	}
	
	@Override
	public void print(AbstractProviderPrinterContext context) {
		
		if (types.size() == 0) {
			initializeProviderMethod();
		}
			
		types.add(context.getRawDto().getCanonicalName());
	}
}