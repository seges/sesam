package sk.seges.sesam.core.pap.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.Types;

import sk.seges.sesam.core.pap.model.api.NamedType;


public class ProcessorUtils {

	public static AnnotationMirror containsAnnotation(Element element, Class<?>... annotations) {
		assert element != null;
		assert annotations != null;

		List<String> annotationClassNames = new ArrayList<String>();
		for ( Class<?> clazz : annotations ) {
			annotationClassNames.add( clazz.getName() );
		}

		List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
		for ( AnnotationMirror mirror : annotationMirrors ) {
			if ( annotationClassNames.contains( mirror.getAnnotationType().toString() ) ) {
				return mirror;
			}
		}
		return null;
	}

	private static Object convertToObject(Object o) {
		if (o instanceof AnnotationValue) {
			AnnotationValue av = (AnnotationValue)o;
			Object oss = av.accept(new SimpleAnnotationValueVisitor6<Object, Void>() {
				@Override
				public Object visitType(TypeMirror t, Void p) {
					try {
						return Class.forName(t.toString());
					} catch (ClassNotFoundException e) {
					}
					return super.visitType(t, p);
				}
				@Override
				protected Object defaultAction(Object o, Void p) {
					return o;
				}
			}, null);
			return oss;
		}
		return o;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> List<T> convertToList(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof List) {
			List<T> result = new ArrayList<T>();
			for (Object object: (List)o) {
				result.add((T)convertToObject(object));
			}
			return result;
		}
		if (o.getClass().isArray()) {
			T[] source = (T[])o;
			
			List<T> result = new ArrayList<T>();
			for (Object object: source) {
				result.add((T)convertToObject(object));
			}
			return result;
		}
		List<T> result = new ArrayList<T>();
		result.add((T)convertToObject(o));
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getAnnotationValue(AnnotationMirror annotationMirror, String parameterValue) {
		assert annotationMirror != null;
		assert parameterValue != null;

		Object returnValue = null;
		for ( Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues()
				.entrySet() ) {
			if ( parameterValue.equals( entry.getKey().getSimpleName().toString() ) ) {
				returnValue = entry.getValue().getValue();
				break;
			}
		}
		
		if (returnValue == null) {
			String annotationName = annotationMirror.getAnnotationType().toString();
			try {
				Class<?> clazz = Class.forName(annotationName);
				Method method = clazz.getMethod(parameterValue);
				if (method == null) {
					return null;
				}
				return (T)method.getDefaultValue();
			} catch (Exception e) {
				return null;
			}
		}
		
		return (T)returnValue;
	}

	public static ExecutableElement getMethodByReturnType(TypeElement typeElement, TypeElement returnType, Types types) {
		List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
		
		for (ExecutableElement method : methods) {
			if (method.getReturnType() != null) {
				//
				if (method.getReturnType().getKind().equals(TypeKind.TYPEVAR)) {
					TypeVariable typeVariable = (TypeVariable)method.getReturnType();
					if (types.isAssignable(returnType.asType(), typeVariable.getUpperBound())) {
						return method;
					}
				} else {
					if (types.isAssignable(returnType.asType(), method.getReturnType())) {
						return method;
					}
				}
			}
		}
		
		return null;
	}
	
	public static ExecutableElement getMethodByParameterType(String name, Element classElement, int paramIndex, TypeMirror parameter, Types types) {
		
		assert name != null;
		assert classElement != null;
		assert parameter != null;
		assert paramIndex >= 0;

		List<ExecutableElement> methods = ElementFilter.methodsIn(classElement.getEnclosedElements());
		
		for (ExecutableElement method : methods) {
			if (!method.getSimpleName().toString().equals(name)) {
				continue;
			}
			if (method.getParameters().size() <= paramIndex) {
				continue;
			}
			
			VariableElement variableElement = method.getParameters().get(paramIndex);
			
			if (types.isAssignable(variableElement.asType(), parameter)) {
				return method;
			}
		}
		
		return null;
	}

	/**
	 * Method determines whether typeElement implements or extends type
	 */
	public static boolean isAssignableFrom(TypeElement typeElement, NamedType type) {
		assert typeElement != null;
		assert type != null;
		
		boolean result;

		result = isOfType(typeElement, type);
		if (result == true) {
			return true;
		}

		List<? extends TypeMirror> interfaces2 = typeElement.getInterfaces();
		TypeMirror superclass = typeElement.getSuperclass();

		result = isAssignable(superclass, type);
		if (result == true) {
			return true;
		}

		for (TypeMirror mirror : interfaces2) {
			if (isAssignable(mirror, type)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAssignable(TypeMirror mirror, NamedType type) {
		assert mirror != null;
		assert type != null;

		if (mirror instanceof DeclaredType) {
			DeclaredType dt = (DeclaredType) mirror;
			TypeElement dte = (TypeElement) dt.asElement();

			if (isOfType(dte, type)) {
				return true;
			} else {
				return isAssignableFrom(dte, type);
			}
		}
		return false;
	}

	public static boolean isOfType(TypeElement te, NamedType type) {
		return te.getQualifiedName().getClass().toString().equals(type.getQualifiedName());
	}

	public static boolean hasMethod(String name, Element classElement) {
		assert name != null;
		assert classElement != null;
		List<ExecutableElement> methods = ElementFilter.methodsIn(classElement.getEnclosedElements());

		for (ExecutableElement method : methods) {
			if (method.toString().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public static TypeMirror getMethodReturnType(String name, Element classElement) {
		assert name != null;
		assert classElement != null;
		
		List<ExecutableElement> methods = ElementFilter.methodsIn(classElement.getEnclosedElements());

		for (ExecutableElement method : methods) {
			if (method.toString().equals(name)) {
				return method.getReturnType();
			}
		}

		return null;
	}
}