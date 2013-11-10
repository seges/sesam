package sk.seges.sesam.pap.model.printer.converter;

import sk.seges.sesam.core.pap.model.mutable.api.*;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;

public class ParameterUsagePrinter {

	private final FormattedPrintWriter pw;

	public ParameterUsagePrinter(FormattedPrintWriter pw) {
		this.pw = pw;
	}

	public void printReferenceDeclaration(MutableType parameterType) {
		if (parameterType instanceof MutableReferenceType) {
			if (((MutableReferenceType) parameterType).getReference() != null) {
				String parameterName = parameterType.toString();

				if (parameterName != null && parameterName.length() > 0 && !((MutableReferenceType) parameterType).isInline()) {
					pw.print(getReferenceType(parameterType));
					pw.print(" ", parameterType.toString(), " = ");
					pw.println(((MutableReferenceType) parameterType).getReference(), ";");
				}
			}
		}
	}

	public boolean isInline(MutableType parameterType) {
		if (parameterType instanceof MutableReferenceType) {
			if (((MutableReferenceType) parameterType).getReference() != null) {
				String parameterName = parameterType.toString();

				return (parameterName != null && parameterName.length() > 0 && ((MutableReferenceType) parameterType).isInline());
			}
		}

		return false;
	}

	public MutableTypeMirror getReferenceType(MutableType parameterType) {
		if (parameterType instanceof MutableReferenceType) {
			if (((MutableReferenceType) parameterType).getReference() != null) {
				String parameterName = parameterType.toString();

				if (parameterName != null && parameterName.length() > 0) {
					MutableTypeValue reference = ((MutableReferenceType) parameterType).getReference();
					if (reference instanceof MutableArrayTypeValue) {
						return ((MutableArrayTypeValue) reference).asType();
					} else if (reference instanceof MutableDeclaredTypeValue) {
						return ((MutableDeclaredTypeValue) reference).asType();
					} else if (reference instanceof MutableReferenceTypeValue) {
						return ((MutableReferenceTypeValue) reference).asType();
					}
				}
			}
		}

		return null;
	}
}
