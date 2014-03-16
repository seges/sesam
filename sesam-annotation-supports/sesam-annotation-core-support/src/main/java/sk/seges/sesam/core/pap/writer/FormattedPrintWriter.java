package sk.seges.sesam.core.pap.writer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableArrayType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableArrayTypeValue;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredTypeValue;
import sk.seges.sesam.core.pap.model.mutable.api.MutableReferenceType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror.MutableTypeKind;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeValue;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.api.MutableWildcardType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.api.DelayedPrintWriter;

public class FormattedPrintWriter extends PrintWriter implements DelayedPrintWriter {

	public static final String DEFAULT_OUDENT = "\t";
	public static final int LINE_LENGTH = 120;

	protected int oudentLevel = 0;
	protected int defaultOudentLevel = 0;
	private boolean startLine = true;

	protected boolean autoIndent = false;
	protected final MutableProcessingEnvironment processingEnv;

	private final OutputStream outputStream;

	protected final String lineSeparator;

	public FormattedPrintWriter(MutableProcessingEnvironment processingEnv) {
		this(new ByteArrayOutputStream(), processingEnv);
	}

	protected FormattedPrintWriter(OutputStream outputStream, MutableProcessingEnvironment processingEnv) {
		super(outputStream);
		this.outputStream = outputStream;
		this.processingEnv = processingEnv;
		this.lineSeparator = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

		if (processingEnv.getPrintSupport() != null) {
			setAutoIndent(processingEnv.getPrintSupport().autoIdent());
			setSerializer(processingEnv.getPrintSupport().printer().printSerializer());
			serializeTypeParameters(processingEnv.getPrintSupport().printer().printTypeParameters());
		} else {
			setAutoIndent(false);
			setSerializer(ClassSerializer.CANONICAL);
			serializeTypeParameters(true);
		}
	}

	public String toString() {
		return toString(getDefaultOudentLevel());
	}
	
	protected String toString(int oudentLevel) {
		String result = "";

		String output = getOutputStream().toString();
		
		String[] lines = output.split(lineSeparator);
		
		boolean endWithNewLine = output.endsWith(lineSeparator);
		
		if (!endWithNewLine && lines.length == 1 && lines[0].trim().length() == 0) {
			return "";
		}
		
		if (lines.length == 0 && endWithNewLine) {
			return null;
		}
		
		int i =0;
		for (String s : lines) {
			result += getOudentation(oudentLevel) + s;
			if (i < lines.length - 1 || endWithNewLine) {
				result += lineSeparator;
			}
			i++;
		}
		
		return result;
	}

	OutputStream getOutputStream() {
		return outputStream;
	}

	public void setDefaultOudentLevel(int defaultOudentLevel) {
		this.defaultOudentLevel = defaultOudentLevel;
	}
	
	public int getDefaultOudentLevel() {
		return defaultOudentLevel;
	}
	
	public void setAutoIndent(boolean autoIndent) {
		this.autoIndent = autoIndent;
	}

	protected void setOudentLevel(int oudentLevel) {
		if (oudentLevel < 0) {
			defaultOudentLevel = defaultOudentLevel + oudentLevel;
			oudentLevel = 0;
		}
		this.oudentLevel = oudentLevel;
	}

	public void indent() {
		if (autoIndent) {
			throw new RuntimeException("Unable to indent manually in auto mode. Please set autoIndent to false.");
		}
		setOudentLevel(getOudentLevel() - 1);
	}

	int getOudentLevel() {
		return oudentLevel;
	}

	public void oudent() {
		if (autoIndent) {
			throw new RuntimeException("Unable to oudent manually in auto mode. Please set autoIndent to false.");
		}
		setOudentLevel(getOudentLevel() + 1);
	}

	private void setAutoOudent(char c) {
		if (c == '{') {
			setOudentLevel(getOudentLevel() + 1);
		}
	}

	private void setAutoIndent(char c) {
		if (c == '}') {
			setOudentLevel(getOudentLevel() - 1);
		}
	}

	private boolean processing = false;

	protected String getOudentation(int oudentation) {
		String result = "";

		for (int i = 0; i < oudentation; i++) {
			result += DEFAULT_OUDENT;
		}
		
		return result;
	}
	
	protected void addIdentation() {
		if (processing) {
			return;
		}
		processing = true;
		if (startLine) {
			String oudentation = getOudentation(getOudentLevel());
			currentPosition += oudentation.length();
			super.print(oudentation);
			startLine = false;
		}
		processing = false;
	}

	private String lastText = null;

	@Override
	public void write(String text, int off, int len) {
		if (!processing) {
			if (currentPosition == 0 && text != null && text.length() > off) {
				setAutoIndent(text.charAt(off));
			}
			addIdentation();
		}
		currentPosition += len;
		super.write(text, off, len);
		if (!processing) {
			if (lastText == null) {
				lastText = "";
			}
			lastText += text.substring(off, len);
		}
	}

	private void newLine() {
		if (lastText != null && autoIndent) {
			for (int i = 0; i < lastText.length(); i++) {
				if (i > 0) {
					setAutoIndent(lastText.charAt(i));
				}
				setAutoOudent(lastText.charAt(i));
			}
		}
		super.println();
		startLine = true;
		lastText = "";
		currentPosition = 0;
	}

	@Override
	public void println() {
		newLine();
	}

	private ClassSerializer serializer = ClassSerializer.CANONICAL;
	private boolean typed = false;

	@Override
	public void setSerializer(ClassSerializer serializer) {
		this.serializer = serializer;
	}

	public void serializeTypeParameters(boolean typed) {
		this.typed = typed;
	}

	@Override
	public void println(Object x) {
		println(new Object[] { x });
	}

	@Override
	public void println(Object... x) {
		print(x);
		println();
	}

	@Override
	public void print(Object obj) {
		print(new Object[] { obj });
	}

	private MutableTypeMirror toMutableType(Object o) {
		if (o instanceof MutableDeclaredType) {
			return (MutableDeclaredType) o;
		} else if (o instanceof MutableArrayType) {
			return (MutableArrayType) o;
		} else if (o instanceof MutableTypeVariable) {
			return (MutableTypeVariable) o;
		} else if (o instanceof MutableWildcardType) {
			return (MutableWildcardType) o;
		} else if (o instanceof MutableArrayTypeValue) {
			return ((MutableArrayTypeValue) o).asType();
		} else if (o instanceof MutableDeclaredTypeValue) {
			return ((MutableDeclaredTypeValue) o).asType();
		}

		return null;
	}

	private String getImportPackage(MutableDeclaredType type) {
		if (type.getEnclosedClass() != null) {
			return type.getEnclosedClass().getCanonicalName();
		}
		return type.getPackageName();
	};

	private boolean isConflictType(MutableTypeValue mutableTypeValue) {
		return false;
	}

	private boolean isConflictType(MutableDeclaredType mutableType) {
		for (MutableDeclaredType importType : processingEnv.getUsedTypes()) {
			if (getImportPackage(importType) != null && importType.getSimpleName().equals(mutableType.getSimpleName())
					&& !getImportPackage(importType).equals(getImportPackage(mutableType))) {
				return true;
			}
		}

		return false;
	}

	private boolean isConflictType(MutableTypeMirror mutableType) {
		Set<MutableDeclaredType> declaredTypes = new TypeExtractor(typed).extractDeclaredType(mutableType);

		// TODO implement better version - print canonical names only those type variables that are in conflict (not
		// whole declared type)
		for (MutableDeclaredType declaredType : declaredTypes) {
			if (isConflictType(declaredType)) {
				return true;
			}
		}

		return false;
	}

	private int currentPosition = 0;

	public int getCurrentPosition() {
		return currentPosition;
	}

	public int printAll(Object[] params) {
		int i = 0;
		for (Object parameter : params) {
			if (i > 0) {
				print(", ");
			}
			print(parameter);
			i++;
		}

		return i;
	}

	@Override
	public void print(Object... x) {

		int length = 0;

		for (Object o : x) {
			if (o instanceof TypeMirror) {
				o = processingEnv.getTypeUtils().toMutableType((TypeMirror) o);
			} else if (o instanceof Element) {
				o = processingEnv.getTypeUtils().toMutableType(((Element) o).asType());
			} else if (o instanceof Class) {
				o = processingEnv.getTypeUtils().toMutableType((Class<?>) o);
			}

			if (o instanceof MutableTypeValue) {

				ClassSerializer evalSerializer = serializer;
				if (serializer.equals(ClassSerializer.SIMPLE)) {
					if (isConflictType((MutableTypeValue) o)) {
						evalSerializer = ClassSerializer.CANONICAL;
					} else {
						processingEnv.getUsedTypes().addAll(extractValueTypes((MutableTypeValue) o));
					}
				}

				String res = ((MutableTypeValue) o).toString(evalSerializer, typed);
				length += res.length();
				super.write(res);
			} else if (o instanceof MutableReferenceType) {
				String name = o.toString();
				if (name != null && name.length() > 0) {
					super.write(name);
				} else {
					print(((MutableReferenceType) o).getReference());
				}
			} else {
				MutableTypeMirror mutableType = toMutableType(o);

				if (mutableType != null) {
					ClassSerializer evalSerializer = serializer;
					if (serializer.equals(ClassSerializer.SIMPLE)) {
						if (isConflictType(mutableType)) {
							evalSerializer = ClassSerializer.CANONICAL;
						} else {
							processingEnv.getUsedTypes().addAll(new TypeExtractor(typed).extractDeclaredType(mutableType));
						}
					}
					String res = mutableType.toString(evalSerializer, typed);
					length += res.length();
					write(res);
				} else {
					String res = String.valueOf(o);
					length += res.length();
					super.write(res);
				}
			}
		}

		currentPosition += length;
	}

	private Set<MutableDeclaredType> extractValueTypes(MutableTypeValue value) {

		Set<MutableDeclaredType> types = new HashSet<MutableDeclaredType>();

		if (value == null) {
			return types;
		}

		if (value instanceof MutableDeclaredTypeValue) {

			// value is clazz
			if (value.getValue() instanceof MutableType) {
				return types;
			}

			if (value.getValue() instanceof Class<?> ) {
				types.add(processingEnv.getTypeUtils().toMutableType((Class<?>)value.getValue()));
				return types;
			}

			MutableTypeMirror type = ((MutableDeclaredTypeValue) value).asType();

			// primitive types
			TypeMirror javaType = processingEnv.getTypeUtils().fromMutableType(type);

			if (type.getKind().equals(MutableTypeKind.PRIMITIVE) || (javaType != null && unboxType(javaType).getKind().isPrimitive())
					|| type.equals(processingEnv.getTypeUtils().toMutableType(String.class))) {
				return types;
			}

			List<Method> methods = getGetterMethods(Arrays.asList(value.getValue().getClass().getDeclaredMethods()));

			for (Method method : methods) {
				try {
					MutableTypeValue typeValue = processingEnv.getTypeUtils().getTypeValue(method.invoke(value.getValue()));
					if (typeValue instanceof MutableArrayTypeValue) {
						types.add((MutableDeclaredType) ((MutableArrayTypeValue) typeValue).asType().getComponentType());
					} else if (typeValue instanceof MutableDeclaredTypeValue) {
						types.add(((MutableDeclaredTypeValue) typeValue).asType());
					}
				} catch (Exception e) {
				}
			}

			types.add(((MutableDeclaredTypeValue) value).asType());
		}

		if (value instanceof MutableArrayTypeValue) {
			types.add((MutableDeclaredType) ((MutableArrayTypeValue) value).asType().getComponentType());

			MutableTypeValue[] arrayValues = ((MutableArrayTypeValue) value).getValue();

			if (arrayValues != null && arrayValues.length > 0) {
				for (int i = 0; i < arrayValues.length; i++) {
					types.addAll(extractValueTypes(arrayValues[i]));
				}
			}
		}

		return types;
	}

	protected TypeMirror unboxType(TypeMirror type) {
		try {
			return processingEnv.getTypeUtils().unboxedType(type);
		} catch (Exception e) {
			return type;
		}
	}

	private List<Method> getGetterMethods(List<Method> methods) {
		List<Method> result = new ArrayList<Method>();

		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				result.add(method);
			}
		}

		return result;
	}
}