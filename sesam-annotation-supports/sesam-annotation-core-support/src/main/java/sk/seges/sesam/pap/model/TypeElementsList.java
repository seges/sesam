package sk.seges.sesam.pap.model;

import javax.lang.model.element.TypeElement;
import java.util.List;

public class TypeElementsList extends AbstractElementsList<TypeElement> {

	public TypeElementsList() {
		super(null);
	}

	@Override
	protected String toString(TypeElement typeElement) {
		return typeElement.getQualifiedName().toString();
	}

}
