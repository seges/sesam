package sk.seges.sesam.core.pap.model.api;

import sk.seges.sesam.core.pap.builder.api.NameTypes.ClassSerializer;

public interface TypeParameter extends NamedType {

	public static final String UNDEFINED = "?";
	
	String getVariable();

	TypeVariable[] getBounds();
	
	String toString(ClassSerializer serializer);
}