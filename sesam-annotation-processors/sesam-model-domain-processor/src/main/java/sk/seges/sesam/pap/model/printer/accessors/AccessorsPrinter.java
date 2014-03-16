package sk.seges.sesam.pap.model.printer.accessors;

import sk.seges.sesam.core.pap.accessor.AnnotationAccessor;
import sk.seges.sesam.core.pap.model.mutable.api.MutableAnnotationMirror;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.printer.AnnotationPrinter;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.printer.copy.CopyPrinter;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccessorsPrinter extends CopyPrinter {

	private final AnnotationPrinter annotationPrinter;
	
	public AccessorsPrinter(MutableProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(processingEnv, pw);
		
		this.annotationPrinter = new AnnotationPrinter(pw, processingEnv);
	}

	protected MutableAnnotationMirror get(List<MutableAnnotationMirror> annotations, AnnotationMirror annotation) {
		for (MutableAnnotationMirror annotationMirror: annotations) {
			if (annotationMirror.getAnnotationType().getCanonicalName().equals(annotation.getAnnotationType().toString())) {
				return annotationMirror;
			}
		}
		return null;
	}

	public List<MutableAnnotationMirror> mergeAnnotations(List<AnnotationMirror> annotations) {
		List<MutableAnnotationMirror> result = new ArrayList<MutableAnnotationMirror>();

		AnnotationAccessor annotationAccessor = new AnnotationAccessor(processingEnv) {

			@Override
			public boolean isValid() {
				return true;
			}
		};

		for (AnnotationMirror annotation: annotations) {

			MutableAnnotationMirror duplicitAnnotation = get(result, annotation);

			if (duplicitAnnotation != null) {
				annotationAccessor.merge(duplicitAnnotation, annotation);
			} else {
				result.add(annotationAccessor.toMutable(annotation));
			}
		}

		return result;
	}

	@Override
	public void print(TransferObjectContext context) {

		if (context.isSuperclassMethod()) {
			return;
		}

		String modifier = Modifier.PUBLIC.toString() + " ";
		
		List<MutableAnnotationMirror> supportedAnnotations = mergeAnnotations(getSupportedAnnotations(context));

		for (MutableAnnotationMirror supportedAnnotation: supportedAnnotations) {
			annotationPrinter.print(supportedAnnotation);
		}
		
		pw.print(modifier, context.getDtoFieldType(), " ");
		
		if (context.getDomainMethod() == null) {
			pw.print(context.getDtoMethod().getSimpleName().toString() + "()");
		} else {
			TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(context.getConfigurationTypeElement().getDomain().getCanonicalName());
			pw.print(MethodHelper.toGetterMethod(typeElement, context.getDtoFieldName()));
		}
		
		pw.println(" {");
		pw.println("return " + context.getDtoFieldName() + ";");
		pw.println("}");
		pw.println();

		pw.println(modifier + "void " + MethodHelper.toSetter(context.getDtoFieldName()) + 
				"(", context.getDtoFieldType(), " " + context.getDtoFieldName() + ") {");
		pw.println("this." + context.getDtoFieldName() + " = " + context.getDtoFieldName() + ";");
		pw.println("}");
		pw.println(" ");
	}
}