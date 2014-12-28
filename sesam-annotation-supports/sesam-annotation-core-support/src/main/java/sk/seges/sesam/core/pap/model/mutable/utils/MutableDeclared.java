package sk.seges.sesam.core.pap.model.mutable.utils;

import java.util.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableExecutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.model.mutable.api.MutableWildcardType;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.structure.api.PackageValidator;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;

//TODO rename to MutableType
class MutableDeclared extends MutableHasAnnotationsType implements MutableDeclaredType {

	protected String simpleName;
	private String packageName;
	private MutableDeclaredType enclosedClass;
	private List<MutableTypeVariable> typeVariables;

	private MutableTypeKind kind;

	private List<MutableTypeMirror> interfaces;
	private List<MutableVariableElement> fields;
	private List<MutableDeclaredType> nestedTypes;
	
	private boolean superClassInitialized = false;
	private MutableDeclaredType superClass;

	private List<Modifier> modifiers;
	private List<MutableExecutableType> mutableMethods = new LinkedList<MutableExecutableType>();

	private MutableExecutableType constructor;

	//TypeMirror for the primitive types, otherwise DeclaredType
	private TypeMirror type;
	private boolean dirty = false;
		
	public MutableDeclared(String packageName, String simpleName, MutableProcessingEnvironment processingEnv) {
		this(null, packageName, simpleName, processingEnv);
	}

	public MutableDeclared(TypeMirror type, String packageName, String simpleName, MutableProcessingEnvironment processingEnv) {
		super(processingEnv, type);
		this.simpleName = simpleName;
		this.packageName = packageName;
		this.type = type;
		this.enclosedClass = null;

		initKind();
	}

	public MutableDeclared(TypeMirror type, MutableDeclaredType enclosedClass, String simpleName, MutableProcessingEnvironment processingEnv) {
		super(processingEnv, type);
		this.simpleName = simpleName;
		this.packageName = enclosedClass.getPackageName();
		this.enclosedClass = enclosedClass;
		this.type = type;

		initKind();
	}
	
	private List<Modifier> ensureModifiers() {
		if (modifiers == null) {
			copyModifiers();
		}
		return modifiers;
	}

	public List<Modifier> getModifiers() {
		return Collections.unmodifiableList(ensureModifiers());
	}

	public MutableDeclaredType setModifier(Modifier... modifiers) {
		dirty();
		ensureModifiers().clear();
		return addModifier(modifiers);
	}
	
	public MutableDeclaredType addModifier(Modifier... modifiers) {
		dirty();
		List<Modifier> result = new ArrayList<Modifier>();
		
		ModifierConverter modifierConverter = new ModifierConverter();
		
		for (Modifier modifier: modifiers) {
			for (Modifier mod: ensureModifiers()) {
				if (modifierConverter.getModifierType(mod) != modifierConverter.getModifierType(modifier)) {
					result.add(mod);
				}
			}
			result.add(modifier);
		}
		
		this.modifiers = result;
		return this;
	}
	
	private void initKind() {
		if (type != null && type.getKind().equals(TypeKind.DECLARED)) {
			this.kind = convertKind(((DeclaredType)type).asElement().getKind());
		} else {
			if (type != null && type.getKind().isPrimitive()) {
				this.kind = MutableTypeKind.PRIMITIVE;
			} else {
				this.kind = MutableTypeKind.CLASS;
			}
		}
	}
	
	private MutableTypeKind convertKind(ElementKind kind) {
		switch (kind) {
		case CLASS:
			return MutableTypeKind.CLASS;
		case INTERFACE:
			return MutableTypeKind.INTERFACE;
		case ANNOTATION_TYPE:
			return MutableTypeKind.ANNOTATION_TYPE;
		case ENUM:
			return MutableTypeKind.ENUM;
		default:
			throw new RuntimeException("Unsupported kind " + kind + ". Unable to create declared type with this kind.");
		}
	}
	
	private void copyModifiers() {
		if (type != null && type.getKind().equals(TypeKind.DECLARED)) {
		
			TypeElement typeElement = (TypeElement)((DeclaredType)type).asElement();	
			if (this.modifiers == null) {
				this.modifiers = new ArrayList<Modifier>();
			}
			this.modifiers.clear();
			this.modifiers.addAll(typeElement.getModifiers());
		} else {
			this.modifiers = new ArrayList<Modifier>();
			this.modifiers.add(Modifier.PUBLIC);
		}
	}

	private void copySuperclass() {
		if (type != null && type.getKind().equals(TypeKind.DECLARED)) {
			TypeElement typeElement = (TypeElement)((DeclaredType)type).asElement();
			
			TypeMirror superclassType = typeElement.getSuperclass();

			if (superclassType.getKind().equals(TypeKind.DECLARED) && !superclassType.toString().equals(Object.class.getName())) {
				this.superClass = processingEnv.getTypeUtils().toMutableType((DeclaredType) superclassType);
			}
		}		
	}
	
	private void copyTypeParameters() {
		if (type != null && type.getKind().equals(TypeKind.DECLARED)) {
			
			List<? extends TypeMirror> typeArguments = ((DeclaredType)type).getTypeArguments();
			
			for (TypeMirror typeArgument: typeArguments) {
				MutableTypeMirror mutableTypeArgument = processingEnv.getTypeUtils().toMutableType(typeArgument);
				if (mutableTypeArgument instanceof MutableTypeVariable) {
					typeVariables.add((MutableTypeVariable)mutableTypeArgument);
				} else {
					typeVariables.add(processingEnv.getTypeUtils().getTypeVariable(null, mutableTypeArgument));
				}
			}
		}
	}

	private void copyInterfaces() {
		if (type != null && type.getKind().equals(TypeKind.DECLARED)) {
			TypeElement typeElement = (TypeElement)((DeclaredType)type).asElement();
			
			for (TypeMirror interfaceType: typeElement.getInterfaces()) {
				interfaces.add(processingEnv.getTypeUtils().toMutableType(interfaceType));
			}
		}
	}
	
	public MutableDeclaredType getEnclosedClass() {
		return enclosedClass;
	};
	
	public MutableDeclaredType setSimpleName(String simpleName) {
		dirty();
		invalidateEnclosedType();
		this.simpleName = simpleName;
		return this;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getPackageName() {
		return packageName;
	}

	private List<MutableTypeVariable> ensureTypeVariables() {
		if (typeVariables == null) {
			typeVariables = new LinkedList<MutableTypeVariable>();
			copyTypeParameters();
		}
		return typeVariables;
	}
	
	public List<MutableTypeVariable> getTypeVariables() {
		return Collections.unmodifiableList(ensureTypeVariables());
	};

	private void dirty() {
		if (type != null) {
			processingEnv.getTypeUtils().invalidateCache(type);
		}
		type = null;
	}
	
	private void invalidateEnclosedType() {
		if (!dirty) {
			enclosedClass = null;
			dirty= true;
		}
	}
	
	public MutableDeclaredType addClassSufix(String sufix) {
		dirty();
		invalidateEnclosedType();
		simpleName += sufix;	
		return this;
	};
	
	public MutableDeclaredType addClassPrefix(String prefix) {
		dirty();
		invalidateEnclosedType();
		simpleName = prefix + simpleName;
		return this;
	};

 	public MutableDeclaredType addPackageSufix(String sufix) {
		dirty();
		invalidateEnclosedType();
		packageName += sufix;
		return this;
	};

	@Override
	public MutableDeclaredType replaceClassSuffix(String originalSuffix, String newSuffix) {
		if(!simpleName.endsWith(originalSuffix)) {
			return this;
		}

		dirty();
		invalidateEnclosedType();
		simpleName = simpleName.substring(0, simpleName.length() - originalSuffix.length()) + newSuffix;
		return this;
	}

	@Override
	public MutableDeclaredType replaceClassPrefix(String originalPrefix, String newPrefix) {
		if(!simpleName.startsWith(originalPrefix)) {
			return this;
		}

		dirty();
		invalidateEnclosedType();
		simpleName = newPrefix + this.simpleName.substring(originalPrefix.length());
		return this;
	}

	@Override
	public MutableDeclaredType removeClassSuffix(String originalSuffix) {
		return replaceClassSuffix(originalSuffix, "");
	}

	@Override
	public MutableDeclaredType removeClassPrefix(String originalPrefix) {
		return replaceClassPrefix(originalPrefix, "");
	}

	public MutableDeclaredType changePackage(String packageName) {
		dirty();
		invalidateEnclosedType();
		this.packageName = packageName;
		return this;
	};
	
	public MutableDeclaredType changePackage(PackageValidator packageValidator) {
		return changePackage(packageValidator.toString());
	};

	public String getCanonicalName() {
		if (enclosedClass != null) {
			return enclosedClass.getCanonicalName() + "." + simpleName;
		}
		return (packageName != null ? (packageName + ".") : "") + simpleName;
	};
	//TODO: check if there should be $ or dot
	public String getQualifiedName() {
		if (enclosedClass != null) {
			return enclosedClass.getCanonicalName() + "$" + simpleName;
		}
		return getCanonicalName();
	};
		
	public TypeMirror asType() {
		return type;
	};


	String toString(MutableDeclared declaredType) {
		if (declaredType.getTypeVariables() == null || declaredType.getTypeVariables().size() == 0) {
			return "";
		}
		
		String types = "<";
		
		int i = 0;
		
		for (MutableTypeVariable typeVariable: declaredType.getTypeVariables()) {
			if (i > 0) {
				types += ", ";
			}
			types += typeVariable.toString();
			i++;
		}
		
		types += ">";
		
		return types;
	}
	
	@Override
	public String toString() {
		return getCanonicalName() + toString(this);
	}

	public String getCanonicalName(boolean typed) {
		return toString(ClassSerializer.CANONICAL, typed);
	}

	public String getSimpleName(boolean typed) {
		return toString(ClassSerializer.SIMPLE, typed);
	}

	public String getQualifiedName(boolean typed) {
		return toString(ClassSerializer.QUALIFIED, typed);
	}

	public String toString(ClassSerializer serializer) {
		switch (serializer) {
		case CANONICAL:
			return getCanonicalName();
		case QUALIFIED:
			return getQualifiedName();
		case SIMPLE:
			return getSimpleName();
		}
		return null;
	}

	@Override
	public String toString(ClassSerializer serializer, boolean typed) {
		
		String resultName = this.toString(serializer);
		
		if (!typed || this.getTypeVariables() == null || this.getTypeVariables().size() == 0) {
			return resultName;
		}

		String types = "<";

		int i = 0;

		for (MutableTypeVariable typeParameter : this.getTypeVariables()) {
			if (i > 0) {
				types += ", ";
			}
			types += typeParameter.toString(serializer, typed);
			i++;
		}

		types += ">";

		return resultName + types;
	}
	
	public MutableTypeKind getKind() {
		return this.kind;
	};

	public MutableDeclaredType setKind(MutableTypeKind kind) {
		dirty();
		this.kind = kind;
		return this;
	}

	public MutableDeclaredType addInterface(MutableTypeMirror interfaceType) {
		if (interfaces == null) {
			interfaces = new ArrayList<MutableTypeMirror>();
			copyInterfaces();
		}

		interfaces.add(interfaceType);

		return this;
	}

	public List<? extends MutableTypeMirror> getInterfaces() {
		if (interfaces == null) {
			interfaces = new ArrayList<MutableTypeMirror>();
			copyInterfaces();
		}
		return Collections.unmodifiableList(interfaces);
	}

	@Override
	public MutableDeclaredType setInterfaces(List<? extends MutableTypeMirror> interfaces) {
		dirty();
		this.interfaces = new ArrayList<MutableTypeMirror>();

		Set<MutableTypeVariable> typeVariables = new HashSet<MutableTypeVariable>();

		for (MutableTypeMirror interfaceType: interfaces) {
			this.interfaces.add(interfaceType);	
			if ((((MutableDeclaredType) interfaceType).hasVariableParameterTypes()) && !hasTypeParameters()) {
				typeVariables.addAll(Arrays.asList(((MutableDeclaredType)interfaceType).getVariableParameterTypes()));
				((MutableDeclaredType)interfaceType).stripTypeParametersTypes();
			}
		}
		
		if (typeVariables.size() > 0) {
			//merge with superclass type variables
			setTypeVariables(typeVariables.toArray(new MutableTypeVariable[] {}));
		}
		
		//TODO handle conflict type variables - with same names
		
		return this;
	}
	
	public MutableDeclaredType getSuperClass() {
		if (!superClassInitialized) {
			copySuperclass();
			superClassInitialized = true;
		}
		return superClass;
	}
	
	public MutableDeclaredType prefixTypeParameter(String prefix) {
		return renameTypeParameter(RenameActionType.PREFIX, prefix + "_");
	}

	public MutableDeclaredType renameTypeParameter(RenameActionType actionType, String parameter) {
		return renameTypeParameter(actionType, parameter, null, false);
	}
	
	private void renameBounds(Set<? extends MutableTypeMirror> bounds, RenameActionType actionType, String parameter, String oldName, boolean recursive) {
		for (MutableTypeMirror bound: bounds) {
			if (bound instanceof MutableDeclaredType && ((MutableDeclaredType)bound).getTypeVariables().size() > 0) {
				((MutableDeclaredType)bound).renameTypeParameter(actionType, parameter, oldName, recursive);
			}
		}
	}
	
	public MutableDeclaredType renameTypeParameter(RenameActionType actionType, String parameter, String oldName, boolean recursive) {	
			
		if (getTypeVariables() == null || getTypeVariables().size() == 0) {
			return this;
		}

		dirty();
		
		for (MutableTypeVariable typeParameter: this.getTypeVariables()) {
			String variable = typeParameter.getVariable();
			
			if (variable != null && !variable.equals(MutableWildcardType.WILDCARD_NAME)) {
				if (oldName == null || oldName.equals(variable)) {
					variable = actionType.apply(variable, parameter);
				}
				typeParameter.setVariable(variable);
			} else if (recursive) {
				renameBounds(typeParameter.getUpperBounds(), actionType, parameter, oldName, recursive);
				renameBounds(typeParameter.getLowerBounds(), actionType, parameter, oldName, recursive);
			}
		}
	
		return this;
	}

	public MutableDeclared stripTypeParameters() {
		dirty();
		this.typeVariables = new LinkedList<MutableTypeVariable>();
		return this;
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((enclosedClass == null) ? 0 : enclosedClass.hashCode());
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((getTypeVariables() == null) ? 0 : getTypeVariables().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutableDeclared other = (MutableDeclared) obj;
		if (enclosedClass == null) {
			if (other.enclosedClass != null)
				return false;
		} else if (!enclosedClass.equals(other.enclosedClass))
			return false;
		if (kind != other.kind)
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (simpleName == null) {
			if (other.simpleName != null)
				return false;
		} else if (!simpleName.equals(other.simpleName))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (getTypeVariables() == null) {
			if (other.getTypeVariables() != null)
				return false;
		} else if (!getTypeVariables().equals(other.getTypeVariables()))
			return false;
		return true;
	}

	@Override
	public MutableDeclaredType setEnclosedClass(MutableDeclaredType type) {
		dirty();
		invalidateEnclosedType();
		this.enclosedClass = type;
		return this;
	}

	@Override
	public MutableDeclaredType cloneTypeVariables(MutableDeclaredType declaredType) {
//		dirty();
		
		this.typeVariables = new LinkedList<MutableTypeVariable>();
		
		for (MutableTypeVariable typeVariable: declaredType.getTypeVariables()) {
			this.typeVariables.add(typeVariable.clone());
		}
		
		return this;
	}

	public MutableDeclaredType setTypeVariables(MutableTypeVariable... mutableTypeVariables) {
		this.typeVariables = new LinkedList<MutableTypeVariable>();
		
		for (MutableTypeVariable typeVariable: mutableTypeVariables) {
			this.typeVariables.add(typeVariable);
		}

        return this;
	}
	
	@Override
	public MutableDeclaredType addTypeVariable(MutableTypeVariable typeVariable) {
//		dirty();
		ensureTypeVariables().add(typeVariable);

        return this;
	}

	public boolean hasTypeParameters() {
		if (typeVariables != null) {
			return typeVariables.size() > 0;
		}
		
		if (type != null && type.getKind().equals(TypeKind.DECLARED)) {
			return ((DeclaredType)type).getTypeArguments().size() > 0;
		}
		
		return false;
	}

	public boolean hasVariableParameterTypes() {
		if (!hasTypeParameters()) {
			return false;
		}
		
		if (typeVariables != null) {
			for (MutableTypeVariable typeParameter: getTypeVariables()) {
				if (typeParameter.getVariable() != null && typeParameter.getVariable().length() > 0 && !typeParameter.getVariable().equals(MutableWildcard.WILDCARD_NAME)) {
					return true;
				}
			}
		} else if (type != null && type.getKind().equals(TypeKind.DECLARED)) {
			for (TypeMirror typeArgument: ((DeclaredType)type).getTypeArguments()) {
				switch (typeArgument.getKind()) {
				case TYPEVAR:
					String variableName = ((TypeVariable)typeArgument).asElement().getSimpleName().toString();
					if (variableName != null && variableName.length() > 0 && !variableName.equals(MutableWildcard.WILDCARD_NAME)) {
						return true;
					}
				default:
					break;
				}
			}
		}
		
		return false;
	}

	public MutableTypeVariable[] getVariableParameterTypes() {
		List<MutableTypeVariable> result = new ArrayList<MutableTypeVariable>();
		
		for (MutableTypeVariable typeParameter: getTypeVariables()) {
			if (typeParameter.getVariable() != null && typeParameter.getVariable().length() > 0) {
				result.add(typeParameter);
			}
		}
		
		return result.toArray(new MutableTypeVariable[] {});
	}

	public MutableDeclaredType stripTypeParametersVariables() {

		boolean invalidate = false;
		
		MutableTypeVariable[] variables = new MutableTypeVariable[getTypeVariables().size()];

		int i = 0;
		for (MutableTypeVariable typeParameter: getTypeVariables()) {
			
			MutableVariable typeVariable = new MutableVariable();

			variables[i] = typeVariable;
			
			if (typeParameter.getLowerBounds() != null) {
				
				Set<MutableTypeMirror> strippedLowerBounds = new LinkedHashSet<MutableTypeMirror>();
				
				for (MutableTypeMirror lowerBound: typeParameter.getLowerBounds()) {
					if (lowerBound instanceof MutableDeclaredType) {
						strippedLowerBounds.add(((MutableDeclaredType)lowerBound).clone().stripTypeParametersVariables());
					} else {
						strippedLowerBounds.add(lowerBound);
					}
				}
				
				typeVariable.setLowerBounds(strippedLowerBounds);
			}
			
			if (typeParameter.getUpperBounds() != null) {
				
				Set<MutableTypeMirror> strippedUpperBounds = new LinkedHashSet<MutableTypeMirror>();

				for (MutableTypeMirror upperBound: typeParameter.getUpperBounds()) {
					if (upperBound instanceof MutableDeclaredType) {
						strippedUpperBounds.add(((MutableDeclaredType)upperBound).clone().stripTypeParametersVariables());
					} else {
						strippedUpperBounds.add(upperBound);
					}
				}
				
				typeVariable.setUpperBounds(strippedUpperBounds);
				
				if (typeVariable.getLowerBounds() == null && typeVariable.getUpperBounds() == null || (typeVariable.getLowerBounds().size() == 0 && typeVariable.getUpperBounds().size() == 0)) {
					typeVariable.setVariable(typeParameter.getVariable());
				}
			}
			
			i++;
		}
		
		if (invalidate) {
			dirty();
		}

		return setTypeVariables(variables);
	}

	private Set<? extends MutableTypeMirror> stripTypeParametersTypes(Set<? extends MutableTypeMirror> bounds) {
		for (MutableTypeMirror bound: bounds) {
			if (bound.getKind().equals(MutableTypeKind.CLASS) ||
				bound.getKind().equals(MutableTypeKind.INTERFACE)) {
				((MutableDeclaredType)bound).stripTypeParametersTypes();
			}
		}
		
		return bounds;
	}

	public MutableDeclaredType stripWildcards() {
		boolean invalidate = false;
		
		MutableTypeVariable[] variables = new MutableTypeVariable[getTypeVariables().size()];
		int i = 0;
		for (MutableTypeVariable typeParameter: getTypeVariables()) {
			if (typeParameter.getVariable() != null && typeParameter.getVariable().equals(MutableWildcardType.WILDCARD_NAME) &&
				(typeParameter.getLowerBounds().size() + typeParameter.getUpperBounds().size()) > 0) {
				MutableVariable typeVariable = new MutableVariable();
				typeVariable.setLowerBounds(stripTypeParametersTypes(typeParameter.getLowerBounds()));
				typeVariable.setUpperBounds(stripTypeParametersTypes(typeParameter.getUpperBounds()));
				typeVariable.setVariable(null);
				variables[i] = typeVariable;
				invalidate = true;
			} else {
				variables[i] = typeParameter;
			}
			i++;
		}

		if (invalidate) {
			dirty();
		}

		return setTypeVariables(variables);
	}
	
	public MutableDeclaredType stripTypeParametersTypes() {

		boolean invalidate = false;
		
		MutableTypeVariable[] variables = new MutableTypeVariable[getTypeVariables().size()];
		int i = 0;
		for (MutableTypeVariable typeParameter: getTypeVariables()) {
			if (typeParameter.getVariable() != null && !typeParameter.getVariable().equals(MutableWildcardType.WILDCARD_NAME)) {
				MutableVariable typeVariable = new MutableVariable();
				typeVariable.setVariable(typeParameter.getVariable().toString());
				variables[i] = typeVariable;
				invalidate = true;
//			} else if (typeParameter.getVariable() != null && typeParameter.getVariable().equals(MutableWildcardType.WILDCARD_NAME) &&
//				(typeParameter.getLowerBounds().size() + typeParameter.getUpperBounds().size()) > 0) {
//				MutableVariable typeVariable = new MutableVariable();
//				typeVariable.setLowerBounds(stripTypeParametersTypes(typeParameter.getLowerBounds()));
//				typeVariable.setUpperBounds(stripTypeParametersTypes(typeParameter.getUpperBounds()));
//				typeVariable.setVariable(null);
//				variables[i] = typeVariable;
//				invalidate = true;
			} else {
				variables[i] = typeParameter;
				stripTypeParametersTypes(typeParameter.getLowerBounds());
				stripTypeParametersTypes(typeParameter.getUpperBounds());
			}
			i++;
		}

		if (invalidate) {
			dirty();
		}

		return setTypeVariables(variables);
	}

	@Override
	public MutableDeclaredType setSuperClass(MutableDeclaredType superClass) {
		dirty();
		this.superClass = superClass;

		if (superClass != null && superClass.hasVariableParameterTypes() && !hasTypeParameters()) {
			setTypeVariables(superClass.getVariableParameterTypes());
			superClass.stripTypeParametersTypes();
		}

		return this;
	}
	
	@Override
	public MutableDeclaredType clone() {
		MutableDeclared result = new MutableDeclared(type, packageName, simpleName, processingEnv);
		result.enclosedClass = enclosedClass;
		annotationHolderDelegate.clone(result.annotationHolderDelegate);
		
		if (typeVariables != null) {
			result.typeVariables = new LinkedList<MutableTypeVariable>();
			for (MutableTypeVariable typeVariable: typeVariables) {
				result.typeVariables.add(typeVariable.clone());
			}
		}
		
		result.dirty = dirty;
		result.kind = kind;
		
		if (interfaces != null) {
			result.interfaces = new ArrayList<MutableTypeMirror>();
			for (MutableTypeMirror interfaceType: interfaces) {
				result.interfaces.add(interfaceType);
			}
		}
		
		if (modifiers != null) {
			result.modifiers = new ArrayList<Modifier>();
			for (Modifier modifier: modifiers) {
				result.modifiers.add(modifier);
			}
		}
		
		result.superClassInitialized = superClassInitialized;
		result.superClass = superClass;

		return result;
	}

	@Override
	public TypeElement asElement() {
		if (asType() == null || !(getKind().equals(MutableTypeKind.ANNOTATION_TYPE) || getKind().equals(MutableTypeKind.CLASS) || getKind().equals(MutableTypeKind.INTERFACE) || getKind().equals(MutableTypeKind.ENUM))) {
			return null;
		}
		
		TypeMirror type = asType();
		
		return (TypeElement)((DeclaredType)type).asElement();
	}

	@Override
	public MutableDeclaredType addField(MutableVariableElement field) {
		dirty();
		if (fields == null) {
			fields = new LinkedList<MutableVariableElement>();
		}
		fields.add(field);
		return this;
	}
	
	@Override
	public MutableVariableElement getField(MutableVariableElement field) {
		if (fields == null) {
			return null;
		}
		for (MutableVariableElement localfield: fields) {
			if (localfield.getSimpleName().equals(field.getSimpleName()) && processingEnv.getTypeUtils().isAssignable(field.asType(), localfield.asType())) {
				return localfield;
			}
		}
		
		return null;
	}

	@Override
	public MutableVariableElement getField(String name) {
		if (fields == null) {
			return null;
		}
		for (MutableVariableElement localfield: fields) {
			if (localfield.getSimpleName().equals(name)) {
				return localfield;
			}
		}

		return null;
	}

	public List<MutableVariableElement> getFields() {
		if (fields == null) {
			return null;
		}
		
		return Collections.unmodifiableList(fields);
	}
	
	public MutableDeclaredType clearFields() {
		dirty();
		if (fields != null) {
			fields.clear();
		}
		return this;
	}

	private HierarchyPrintWriter printWriter;
	
	@Override
	public HierarchyPrintWriter getPrintWriter() {
		if (printWriter == null) {
			printWriter = new HierarchyPrintWriter(processingEnv);
		}
		
		return printWriter;
	}

	@Override
	public MutableDeclaredType addMethod(MutableExecutableType method) {
		mutableMethods.add(method);
		return this;
	}

	@Override
	public List<MutableExecutableType> getMethods() {
		return Collections.unmodifiableList(mutableMethods);
	}

	@Override
	public MutableExecutableType getMethod(String name) {
		if (mutableMethods == null) {
			return null;
		}

		for (MutableExecutableType method: mutableMethods) {
			if (method.getSimpleName().equals(name)) {
				return method;
			}
		}

		return null;
	}

	@Override
	public MutableExecutableType getConstructor() {
		if (constructor == null) {
			MutableMethod constructorMethod = new MutableMethod(processingEnv, getSimpleName());
			constructorMethod.addModifier(Modifier.PUBLIC);
			constructorMethod.setDefault(true);
			constructor =  constructorMethod;
		}
		
		return constructor;
	}

	@Override
	public MutableDeclaredType addNestedType(MutableDeclaredType nestedType) {
		if (nestedTypes == null) {
			nestedTypes = new ArrayList<MutableDeclaredType>();
		}
		
		nestedTypes.add(nestedType);
		
		return nestedType;
	}

	@Override
	public List<MutableDeclaredType> getNestedTypes() {
		if (nestedTypes == null) {
			return new ArrayList<MutableDeclaredType>();
		}
		return Collections.unmodifiableList(nestedTypes);
	}	
}