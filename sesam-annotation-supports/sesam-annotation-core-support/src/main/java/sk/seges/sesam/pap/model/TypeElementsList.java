package sk.seges.sesam.pap.model;

import javax.lang.model.element.TypeElement;

public class TypeElementsList extends AbstractElementsList<TypeElement> {

	@Override
	protected String toString(TypeElement typeElement) {
		return typeElement.getQualifiedName().toString();
	}

}
