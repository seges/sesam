package sk.seges.sesam.pap.model.printer.clone;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.accessor.GenerateCloneAccessor;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.printer.AbstractElementPrinter;

import javax.tools.Diagnostic;
import java.util.Set;

public class ClonePrinter extends AbstractElementPrinter {

	private final MutableProcessingEnvironment processingEnv;

	private static final String RESULT_NAME = "result";

	private boolean active = true;

	public ClonePrinter(MutableProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(pw);
		this.processingEnv = processingEnv;
	}

	@Override
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputName) {

		active = new GenerateCloneAccessor(configurationTypeElement.asConfigurationElement(), processingEnv).generate();

		if (!active) {
			return;
		}

		DtoDeclaredType dto = configurationTypeElement.getDto();

		pw.println("public ", dto, " clone() {");
		pw.println(dto, " " + RESULT_NAME + " = new " + dto + "();");
		pw.println();
	}

	@Override
	public void print(TransferObjectContext context) {
		if (!active) {
			return;
		}

		switch (context.getDtoFieldType().getKind()) {
			case ENUM:
			case PRIMITIVE:
				pw.println(RESULT_NAME + "." + MethodHelper.toSetter(context.getDtoFieldName()) + "(" + MethodHelper.toGetter(context.getDtoFieldName()) + ");");
				return;
			case CLASS:
			case INTERFACE:
				MutableDeclaredType iterableType = processingEnv.getTypeUtils().toMutableType(Iterable.class);

				if (processingEnv.getTypeUtils().implementsType(context.getDtoFieldType(), iterableType)) {
					String newFieldName = "new" + MethodHelper.toMethod(context.getDomainFieldName());
					pw.println(context.getDtoFieldType() + newFieldName + " = null;" );
					pw.println("if (" + MethodHelper.toGetter(context.getDtoFieldName()) + " != null) {");
					//TODO sometimes it could be interface which is not instantiable, so we have to use
					//default implementation
					pw.println(newFieldName + " = new " + context.getDtoFieldType() + "();");

					MutableTypeMirror elementType = processingEnv.getTypeUtils().toMutableType(Object.class);

					if (context.getDtoFieldType().getKind().equals(MutableTypeMirror.MutableTypeKind.TYPEVAR)) {
						Set<? extends MutableTypeMirror> lowerBounds = ((MutableTypeVariable) context.getDtoFieldType()).getLowerBounds();

						if (lowerBounds != null && lowerBounds.size() > 0) {
							if (lowerBounds.size() > 1) {
								processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[WARNING] More type variables are defined in iterable implementation " +
										context.getDtoFieldType() + ". Using first one!", context.getConfigurationTypeElement().asConfigurationElement());

							}
							elementType = lowerBounds.iterator().next();
						}

						Set<? extends MutableTypeMirror> upperBounds = ((MutableTypeVariable) context.getDtoFieldType()).getUpperBounds();

						if (upperBounds != null && upperBounds.size() > 0) {
							if (upperBounds.size() > 1) {
								if (lowerBounds.size() > 1) {
									processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[WARNING] More type variables are defined in iterable implementation " +
											context.getDtoFieldType() + ". Using first one!", context.getConfigurationTypeElement().asConfigurationElement());

								}
							}
							elementType = upperBounds.iterator().next();
						}
					}

					pw.println("for (", elementType, " _element: " + MethodHelper.toGetter(context.getDtoFieldName()) + " {");
					pw.println(newFieldName + "." + MethodHelper.toSetter(context.getDomainFieldName()) + "(_element);");
					pw.println("}");
					pw.println("}");
				} else {
					pw.println(RESULT_NAME + "." + MethodHelper.toSetter(context.getDtoFieldName()) + "(" + MethodHelper.toGetter(context.getDtoFieldName()) + ");");
				}
				return;
			case ARRAY:
				//TODO implement me!
				processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[ERROR] Not implemented - arrays not yet supported - " + context.getDtoFieldName(),
						context.getConfigurationTypeElement().asConfigurationElement());
				return;
			case WILDCARD:
			case VOID:
			case TYPEVAR:
				processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "[WARNING] Unsupported type " + context.getDtoFieldName() + " (" + context.getDtoFieldType().getKind() + ") in the " +
						context.getConfigurationTypeElement().asConfigurationElement(), context.getConfigurationTypeElement().asConfigurationElement());
				return;
		}
	}

	@Override
	public void finish(ConfigurationTypeElement configurationTypeElement) {

		if (!active) {
			return;
		}

		pw.println();
		pw.println("return " + RESULT_NAME + ";");
		pw.println("}");
		pw.println();
	}
}