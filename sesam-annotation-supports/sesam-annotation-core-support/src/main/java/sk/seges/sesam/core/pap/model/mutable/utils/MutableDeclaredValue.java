package sk.seges.sesam.core.pap.model.mutable.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredTypeValue;
import sk.seges.sesam.core.pap.model.mutable.api.MutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror.MutableTypeKind;
import sk.seges.sesam.core.reflection.comparator.MethodComparator;

class MutableDeclaredValue extends MutableValue implements MutableDeclaredTypeValue {

	private MutableDeclaredType type;
	private MutableProcessingEnvironment processingEnv;
	
	public MutableDeclaredValue(MutableDeclaredType type, Object value, MutableProcessingEnvironment processingEnv) {
		super(value);
		this.type = type;
		this.processingEnv = processingEnv;
	}

	@Override
	public MutableDeclaredType asType() {
		return type;
	}
	
	@Override
	public String toString() {
		return toString(ClassSerializer.CANONICAL);
	}
	
	@Override
	public String toString(ClassSerializer serializer) {
		return toString(serializer, true);
	}

	protected TypeMirror unboxType(TypeMirror type) {
		try {
			return processingEnv.getTypeUtils().unboxedType(type);
		} catch (Exception e) {
			return type;
		}
	}

	@Override
	public String toString(ClassSerializer serializer, boolean typed) {
		if (value == null) {
			return "null";
		}

		//value is clazz
		if (value instanceof MutableType) {
			return ((MutableType)value).toString(serializer, typed);
		}

		if (value instanceof Class) {
			return processingEnv.getTypeUtils().toMutableType(((Class) value)).toString(serializer, typed) + ".class";
		}

		//value is string
		if (value instanceof String) {

			String val = value.toString().replaceAll("\\\\", "\\\\\\\\");

			return "\"" + val + "\"";
		}

		//primitive types
		if (type.getKind().equals(MutableTypeKind.PRIMITIVE)) {
			return value.toString();
		}

		if (unboxType(processingEnv.getTypeUtils().fromMutableType(type)).getKind().isPrimitive()) {
			return value.toString();
		}

		//annotation value
		String result = "new " + type.toString(serializer, typed) + "(";
		
		List<Method> methods = getGetterMethods(Arrays.asList(value.getClass().getDeclaredMethods()));
		Collections.sort(methods, new MethodComparator());
		
		int i = 0;
		for (Method method: methods) {
			if (i > 0) {
				result += ", ";
			}
			try {
				result += processingEnv.getTypeUtils().getTypeValue(method.invoke(value)).toString(serializer, typed);
				i++;
			} catch (Exception e) {}
		}
		
		result += ")";
		
		return result;
	}

	private List<Method> getGetterMethods(List<Method> methods) {
		
		List<Method> result = new ArrayList<Method>();
		
		for (Method method: methods) {
			if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
				result.add(method);
			}
		}
		
		return result;
	}
}