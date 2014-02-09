package sk.seges.sesam.pap.configuration.processor;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sk.seges.sesam.core.configuration.annotation.Configuration;
import sk.seges.sesam.core.configuration.annotation.Settings;
import sk.seges.sesam.core.pap.configuration.api.ProcessorConfigurer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.configuration.configurer.SettingsProcessorConfigurer;
import sk.seges.sesam.pap.configuration.model.AbstractParameterHandler;
import sk.seges.sesam.pap.configuration.model.setting.SettingsIterator;
import sk.seges.sesam.pap.configuration.model.setting.SettingsTypeElement;
import sk.seges.sesam.pap.configuration.printer.*;
import sk.seges.sesam.pap.configuration.printer.ConstructorBodyPrinter;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class SettingsProcessor extends PojoAnnotationTransformerProcessor {

	@Override
	protected ProcessorConfigurer getConfigurer() {
		return new SettingsProcessorConfigurer();
	}

	@Override
	protected MutableDeclaredType[] getOutputClasses(RoundContext context) {
		return new MutableDeclaredType[] { new SettingsTypeElement((DeclaredType)context.getTypeElement().asType(), processingEnv) };
	}
	
	private boolean isEnclosedToConfiguration(DeclaredType type) {
		TypeMirror enclosingType = type.getEnclosingType();
		while (enclosingType.getKind().equals(TypeKind.DECLARED)) {
			type = (DeclaredType)enclosingType;
			//TODO read from configuration
			if (type.asElement().getAnnotation(Configuration.class) != null) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean checkPreconditions(ProcessorContext context, boolean alreadyExists) {
		if (isEnclosedToConfiguration((DeclaredType)context.getTypeElement().asType())) {
			return false;
		}
		return super.checkPreconditions(context, alreadyExists);
	}

	@Override
	protected void printAnnotations(ProcessorContext context) {
		context.getPrintWriter().println("@", Settings.class, "(configuration = ", context.getTypeElement(), ".class)");
		super.printAnnotations(context);
	}

	public void processAnnotation(TypeElement typeElement, MutableDeclaredType outputName, FormattedPrintWriter pw) {

		for (ElementKind supportedType: getSupportedTypes()) {
			for (AbstractSettingsElementPrinter printer: getElementPrinters(pw, supportedType)) {
				printer.initialize(typeElement, outputName);
	
				SettingsIterator settingsIterator = new SettingsIterator(typeElement, supportedType, processingEnv);
				while (settingsIterator.hasNext()) {
					AbstractParameterHandler handler = settingsIterator.next();
					handler.handle(printer);
				}
	
				printer.finish(typeElement);
			}
		}		
	}

	protected AbstractSettingsElementPrinter[] getElementPrinters(FormattedPrintWriter pw) {
		return new AbstractSettingsElementPrinter[] {
			new NestedParameterPrinter(pw, this, processingEnv),
			new FieldPrinter(pw, processingEnv),
			new GroupPrinter(processingEnv,
						new JavaDocPrinter(pw, processingEnv),
						new AccessorPrinter(pw, processingEnv)),
			new ConfigurationValueConstructorPrinter(pw, processingEnv),
			new ConstructorDefinitionPrinter(pw, processingEnv),
			new ConstructorBodyPrinter(pw, processingEnv),
			new CopyConstructorDefinitionPrinter(pw, processingEnv),
			new MergePrinter(pw, processingEnv),
			new HelperPrinter(pw, processingEnv) 
		};
	}			
}