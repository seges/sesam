package sk.seges.sesam.pap.model.annotation;

public @interface ReadOnly {

	public enum PropertyType {
		FIELD, METHOD;
	}

	PropertyType value() default PropertyType.FIELD;
}