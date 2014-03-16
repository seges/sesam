package sk.seges.sesam.core.pap.model.mutable.utils;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableArrayType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeValue;

class MutableAnnotationArrayValue extends MutableArrayValue {

	public MutableAnnotationArrayValue(MutableArrayType type, MutableTypeValue[] value) {
		super(type, value);
	}

	@Override
	public String toString(ClassSerializer serializer, boolean typed) {
		return paramsToString(" {", serializer, typed);
	}
}