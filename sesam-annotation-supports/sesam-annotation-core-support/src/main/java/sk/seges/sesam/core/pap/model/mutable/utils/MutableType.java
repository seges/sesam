package sk.seges.sesam.core.pap.model.mutable.utils;

import java.lang.reflect.Type;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;

abstract class MutableType implements Type, MutableTypeMirror {

	protected MutableType() {}

	@Override
	public boolean isSameType(MutableTypeMirror type) {
		return toString(ClassSerializer.CANONICAL, false).equals(type.toString(ClassSerializer.CANONICAL, false));
	}
}