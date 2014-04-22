package sk.seges.sesam.pap.model.printer.method;

import sk.seges.sesam.core.pap.model.PathResolver;
import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror.MutableTypeKind;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.ConverterProcessingHelper;
import sk.seges.sesam.pap.model.accessor.ReadOnlyAccessor;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.*;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.printer.api.TransferObjectElementPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;
import sk.seges.sesam.utils.CastUtils;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import java.util.Set;

public class CopyFromDtoMethodPrinter extends AbstractMethodPrinter implements CopyMethodPrinter {

	private final Set<String> instances;

	public CopyFromDtoMethodPrinter(Set<String> instances, ConverterProviderPrinter converterProviderPrinter, EntityResolver entityResolver,
			ConverterConstructorParametersResolverProvider parametersResolverProvider, RoundEnvironment roundEnv,
			TransferObjectProcessingEnvironment processingEnv) {
		super(converterProviderPrinter, parametersResolverProvider, entityResolver, roundEnv, processingEnv);
		this.instances = instances;
	}

	@Override
	public void printCopyMethod(TransferObjectContext context, FormattedPrintWriter pw) {

        if (isId(context)) {
			return;
		}

		if (context.isSuperclassMethod()) {
			DomainDeclaredType superClass = context.getConfigurationTypeElement().getInstantiableDomain().getSuperClass();
		 	if (superClass == null || ConverterProcessingHelper.isConverterGenerated(superClass.getDomainDefinitionConfiguration().asConfigurationElement(), processingEnv)) {
				return;
			}
		}

		if (new ReadOnlyAccessor(context.getDtoMethod(), processingEnv).isReadonly()) {
			return;
		}

        PathResolver pathResolver = new PathResolver(context.getDomainFieldPath());

        if (getSetterMethod(context, pathResolver) == null) {
			return;
		}

        boolean nested = pathResolver.isNested();

		String currentPath = pathResolver.next();
		String fullPath = currentPath;
		String previousPath = TransferObjectElementPrinter.RESULT_NAME;

		DomainDeclaredType domainTypeElement = context.getConfigurationTypeElement().getDomain();
		DomainDeclaredType instantiableDomainTypeElement = context.getConfigurationTypeElement().getInstantiableDomain();

		if (nested && context.getConfigurationTypeElement().getDomain() != null) {

			DomainDeclaredType referenceDomainType = domainTypeElement;

			String dtoName = TransferObjectElementPrinter.DTO_NAME;

			while (pathResolver.hasNext()) {

				DomainType instantiableDomainReference = referenceDomainType.getDomainReference(entityResolver, currentPath);

				if (instantiableDomainReference == null) {
					processingEnv.getMessager().printMessage(Kind.ERROR,
							"[ERROR] Unable to find getter method for the field " + currentPath + " in the " + domainTypeElement.toString(),
							context.getConfigurationTypeElement().asConfigurationElement());
					return;
				}

				DomainType domainReference = instantiableDomainReference;

				if (instantiableDomainReference.getConfigurations().size() > 0) {
					domainReference = domainReference.getConfigurations().get(0).getDomain();
				}

				if (!instantiableDomainReference.getKind().isDeclared()) {
					processingEnv.getMessager().printMessage(
							Kind.ERROR,
							"[ERROR] Invalid mapping specified in the field " + currentPath + ". Current path (" + fullPath
									+ ") address getter type that is not class/interfaces." + "You probably mistyped this field in the configuration.",
							context.getConfigurationTypeElement().asConfigurationElement());

					return;
				}

				referenceDomainType = (DomainDeclaredType) domainReference;

				if (!instances.contains(fullPath)) {
					// TODO check if getId is null

					if (referenceDomainType.getKind().isDeclared()) {
						MutableDeclaredType fieldType = processingEnv.getTypeUtils().getDeclaredType(processingEnv.getTypeUtils().toMutableType(Class.class),
								new MutableDeclaredType[] { referenceDomainType.getDto() });

						Field field = new Field("" + referenceDomainType.getDto().getSimpleName() + ".class", fieldType);

						printCopyNested(pathResolver, fullPath, referenceDomainType, context.getDomainMethod(), pw, field, dtoName);
						instances.add(fullPath);
					}

					if (instances.contains(currentPath)) {
						pw.println(previousPath + "." + MethodHelper.toSetter(currentPath) + "(" + currentPath + ");");
					}
				}

				previousPath = currentPath;
				currentPath = pathResolver.next();
				fullPath += MethodHelper.toMethod(currentPath);

				dtoName = previousPath;
			}

			if (domainTypeElement != null && domainTypeElement.getSetterMethod(context.getDomainFieldPath()) != null) {
				printCopy(pathResolver, context, pw);
			} else if (!entityResolver.isImmutable(instantiableDomainTypeElement.asElement())) {
				ExecutableElement domainGetterMethod = instantiableDomainTypeElement.getGetterMethod(currentPath);

				VariableElement field = MethodHelper.getField(instantiableDomainTypeElement.asConfigurationElement(), currentPath);

				if ((domainGetterMethod == null && field != null && !entityResolver.isIdField(field)) || !entityResolver.isIdMethod(domainGetterMethod)) {
					processingEnv.getMessager().printMessage(Kind.ERROR,
							"[ERROR] Setter is not available for the field " + currentPath + " in the class " + instantiableDomainTypeElement.toString(),
							context.getConfigurationTypeElement().asConfigurationElement());
				}
			}
		} else {
			printCopy(pathResolver, context, pw);
		}
	}

	protected void printCopy(PathResolver pathResolver, TransferObjectContext context, FormattedPrintWriter pw) {
		if (context.getConverter() != null) {
			printCopyByConverter(context, pathResolver, pw);
		} else if (context.useConverter()) {
			String converterName = "converter" + MethodHelper.toMethod("", context.getDtoFieldName());
			pw.print(converterProviderPrinter.getDtoConverterType(context.getDomainMethodReturnType(), true), " " + converterName + " = ");
			converterProviderPrinter.printObtainConverterFromCache(pw, ConverterTargetType.DTO, context.getDomainMethodReturnType(), new Field(
					TransferObjectElementPrinter.DTO_NAME + "." + MethodHelper.toGetter(context.getDtoFieldName()), null), context.getDomainMethod(), true);
			pw.println(";");
			printCopyByLocalConverter(converterName, pathResolver, context.getDomainMethodReturnType(), context.getDtoFieldName(), pw);
		} else if (!pathResolver.isNested()) {
			printCopySimple(pathResolver, context, pw);
		}
	}

	protected void printCopyNested(PathResolver domainPathResolver, String fullPath, DomainDeclaredType referenceDomainType, ExecutableElement method,
			FormattedPrintWriter pw, Field field, String dtoName) {

        DomainDeclaredType instantiableDomain = (DomainDeclaredType)referenceDomainType.getConverter().getInstantiableDomain();

        pw.print(referenceDomainType, " " + domainPathResolver.getCurrent() + " = ");

		if (instantiableDomain.getId(entityResolver) != null) {
			if (referenceDomainType.getConverter() == null) {
				processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] No converter/configuration for " + referenceDomainType + " was found. Please, define configuration for "
                        + referenceDomainType);
			}
			converterProviderPrinter.printObtainConverterFromCache(pw, ConverterTargetType.DTO, referenceDomainType, field, method, true);

			pw.println(".createDomainInstance(" + dtoName + "."
					+ MethodHelper.toGetter(fullPath + MethodHelper.toMethod(MethodHelper.toField(instantiableDomain.getIdMethod(entityResolver)))) + ");");
		} else {
			pw.println(TransferObjectElementPrinter.RESULT_NAME + "." + MethodHelper.toGetter(domainPathResolver.getCurrent()) + ";");
			pw.println("if (" + domainPathResolver.getCurrent() + " == null) {");
			pw.print(domainPathResolver.getCurrent() + " = ");
			// TODO add NPE check
			converterProviderPrinter.printObtainConverterFromCache(pw, ConverterTargetType.DTO, referenceDomainType, field, method, true);
			pw.println(".createDomainInstance(null);");
			pw.println("}");
		}
	}

	protected void printCopySimple(PathResolver domainPathResolver, TransferObjectContext context, FormattedPrintWriter pw) {

		DomainDeclaredType domainTypeElement = context.getConfigurationTypeElement().getInstantiableDomain();

		Boolean isMethod = false;
		ExecutableElement domainGetterMethod;
		if (domainTypeElement.asElement() != null
				&& ProcessorUtils.hasMethod(MethodHelper.toMethod(MethodHelper.GETTER_IS_PREFIX, domainPathResolver.getCurrent()),
						domainTypeElement.asElement())) {
			isMethod = true;
			domainGetterMethod = domainTypeElement.getGetterMethod(domainPathResolver.getCurrent());
		} else {
			domainGetterMethod = domainTypeElement.getGetterMethod(domainPathResolver.getCurrent());
		}

		ConfigurationTypeElement configurationTypeElement = context.getConfigurationTypeElement();

		if (configurationTypeElement.getInstantiableDomain().getSetterMethod(domainPathResolver.getPath()) != null) {
			boolean castToInstance = configurationTypeElement.getDomain().getSetterMethod(domainPathResolver.getPath()) == null;

			if (castToInstance) {
				pw.print("((", configurationTypeElement.getInstantiableDomain(), ")");
			}
			pw.print(TransferObjectElementPrinter.RESULT_NAME);

			if (castToInstance) {
				pw.print(")");
			}
			pw.print("." + MethodHelper.toSetter(domainPathResolver.getPath()) + "(");

			if (context.getDomainMethodReturnType() instanceof MutableTypeVariable) {
				pw.print("(" + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX + "_"
						+ ((MutableTypeVariable) context.getDomainMethodReturnType()).getVariable() + ")");
			}

			String dtoField = context.getDtoFieldName();

			pw.print(TransferObjectElementPrinter.DTO_NAME + "." + ((isMethod) ? MethodHelper.toIsGetter(dtoField) : MethodHelper.toGetter(dtoField)));
			pw.println(");");
		} else if (!entityResolver.isImmutable(domainTypeElement.asElement())) {

			VariableElement field = MethodHelper.getField(domainTypeElement.asConfigurationElement(), domainPathResolver.getCurrent());

			if ((domainGetterMethod == null && field != null && !entityResolver.isIdField(field)) || !entityResolver.isIdMethod(domainGetterMethod)) {
				processingEnv.getMessager().printMessage(Kind.ERROR,
						"[ERROR] Setter is not available for the field " + domainPathResolver.getCurrent() + " in the class " + domainTypeElement.toString(),
						configurationTypeElement.asConfigurationElement());
				return;
			}
		}
	}

	protected void printCopyByConverter(TransferObjectContext context, PathResolver domainPathResolver, FormattedPrintWriter pw) {
		String converterName = "converter" + MethodHelper.toMethod("", context.getDtoFieldName());
		pw.print(context.getConverter().getConverterBase(), " " + converterName + " = ");

		Field field = new Field((context.getDomainMethodReturnType().getKind().equals(MutableTypeKind.TYPEVAR) ? "(" + context.getConverter().getDto() + ")"
				: "") + TransferObjectElementPrinter.DTO_NAME + "." + MethodHelper.toGetter(context.getDtoFieldName()), context.getConverter().getDto());
		TransferObjectMappingAccessor transferObjectMappingAccessor = new TransferObjectMappingAccessor(context.getDtoMethod(), processingEnv);
		if (transferObjectMappingAccessor.isValid() && transferObjectMappingAccessor.getConverter() != null) {
			converterProviderPrinter.printDtoGetConverterMethodName(context.getConverter().getDto(), field, context.getDtoMethod(), pw, false);
		} else {
			converterProviderPrinter.printObtainConverterFromCache(pw, ConverterTargetType.DTO, context.getConverter().getDomain(), field,
					context.getDomainMethod(), true);
		}
		pw.println(";");

		pw.println("if (" + converterName + " != null) {");
		pw.print(TransferObjectElementPrinter.RESULT_NAME + "." + MethodHelper.toSetter(domainPathResolver.getPath()) + "(");

		if (context.getDomainMethodReturnType().getKind().equals(MutableTypeKind.TYPEVAR)) {
			pw.print("(" + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX + "_"
					+ ((MutableTypeVariable) context.getDomainMethodReturnType()).getVariable().toString() + ")");
		}

		MutableTypeMirror parameterType = getParameterType(context, domainPathResolver);
		
		if (isCastRequired(parameterType)) {
			pw.print(CastUtils.class, ".cast(");
		}
		pw.print("(", getWildcardDelegate(context.getConverter().getDomain()), ")");
		pw.print(converterName + ".fromDto(");
		// TODO check for the nested
		// TODO: only if necessary
		if (isCastRequired(parameterType)) {
			pw.print(CastUtils.class, ".cast(");
		} else {
			pw.print("(", context.getConverter().getDto() ,")");
		}
		pw.print(TransferObjectElementPrinter.DTO_NAME + "." + MethodHelper.toGetter(context.getDtoFieldName()));
		if (isCastRequired(parameterType)) {
			pw.print(", ", getTypeVariableDelegate(getDelegateCast(context.getConverter().getDto(), true)), ".class)");
		}
		
		pw.print(")");
		if (isCastRequired(parameterType)) {
			pw.print(", ");
			printCastDomainType(context, domainPathResolver, processingEnv.getTransferObjectUtils().getDomainType(parameterType), pw);
			pw.print(".class)");
		}
		pw.println(");");
		pw.println("}");
	}

	protected boolean isCastRequired(MutableTypeMirror type) {
		if (type.getKind().equals(MutableTypeKind.CLASS) || type.getKind().equals(MutableTypeKind.INTERFACE)) {
			return ((MutableDeclaredType)type).hasTypeParameters() && ((MutableDeclaredType)type).getTypeVariables().size() == 1;
		}
		return false;
	}

    protected ExecutableElement getSetterMethod(TypeElement typeElement, PathResolver domainPathResolver) {
        ExecutableElement method = null;

        domainPathResolver = new PathResolver(domainPathResolver.getPath());

        while (domainPathResolver.hasNext()) {
            String path = domainPathResolver.next();

            method = ProcessorUtils.getMethod(MethodHelper.toSetter(path), typeElement);

            if (method == null || method.getParameters().size() == 0) {
                return null;
            }

            TypeMirror typeMirror = method.getParameters().get(0).asType();

            switch (typeMirror.getKind()) {
                case DECLARED:
                    typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
                    break;
                case TYPEVAR:
                    TypeMirror erasure = ProcessorUtils.erasure(typeElement, typeMirror.toString());
                    if (erasure == null) {
                        for (TypeParameterElement parameter: typeElement.getTypeParameters()) {
                            if (parameter.getSimpleName().toString().equals(typeMirror.toString())) {
                                //TODO if there are more bounds?
                                erasure = parameter.getBounds().get(0);
                                break;
                            }
                        }

                        if (erasure == null) {
                            throw new RuntimeException("Unable to erasure " + typeMirror.toString() + " in " + typeElement.getQualifiedName());
                        }
                    }

                    typeElement = (TypeElement) ((DeclaredType) erasure).asElement();
                    break;
                case ARRAY:
                    throw new RuntimeException("Support for arrays is not implemented! Implement me");
                case BOOLEAN:
                case BYTE:
                case CHAR:
                case DOUBLE:
                case FLOAT:
                case INT:
                case LONG:
                case SHORT:
                    if (domainPathResolver.hasNext()) {
                        throw new RuntimeException("Basic types cannot be nested " + domainPathResolver.getPath() + " is invalid path");
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported parameter type " + typeMirror.getKind());
            }
        }

        return method;
    }

    protected ExecutableElement getSetterMethod(TransferObjectContext context, PathResolver domainPathResolver) {
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(context.getConfigurationTypeElement().getDomain().getCanonicalName());
        return getSetterMethod(typeElement, domainPathResolver);

        //return ProcessorUtils.getMethod(MethodHelper.toSetter(domainPathResolver.getPath()), typeElement));
	}
	
	protected MutableTypeMirror getParameterType(TransferObjectContext context, PathResolver domainPathResolver) {
		return processingEnv.getTypeUtils().toMutableType(getSetterMethod(context, domainPathResolver).getParameters().get(0).asType());
	}
	
	protected void printCastDomainType(TransferObjectContext context, PathResolver domainPathResolver, DomainType inputDomainType, FormattedPrintWriter pw) {
		
		MutableTypeMirror parameterType = getParameterType(context, domainPathResolver);
		
		MutableTypeMirror domainType = getTypeVariableDelegate(getDelegateCast(inputDomainType, true));

		if (!isCastRequired(parameterType)) {
			pw.print(domainType);
		} else {
			MutableDeclaredType declaredParameter = (MutableDeclaredType) parameterType;
			MutableTypeMirror typeParameter = getTypeParameter(declaredParameter);

			if (typeParameter == null) {
				pw.print(domainType);
			} else {

				if (typeParameter.isSameType(domainType)
						|| ProcessorUtils.implementsType(processingEnv.getElementUtils().getTypeElement(domainType.toString(ClassSerializer.CANONICAL, false))
								.asType(), processingEnv.getElementUtils().getTypeElement(typeParameter.toString(ClassSerializer.CANONICAL, false)).asType())) {
					pw.print(domainType);
				} else {
					processingEnv.getMessager().printMessage(Kind.NOTE, "Params: [1] InputDomainType: " + inputDomainType.toString());
					processingEnv.getMessager().printMessage(
							Kind.ERROR,
							"Method " + getSetterMethod(context, domainPathResolver).getSimpleName().toString() + " in type "
									+ context.getConfigurationTypeElement().getDomain().getCanonicalName() + " has parameter of type "
									+ typeParameter.toString() + " but " + domainType.toString() + " was expected!");
				}
			}
		}
	}
	
	protected MutableTypeMirror getTypeParameter(MutableDeclaredType type) {
		if (!type.hasTypeParameters()) {
			return null;
		}
		MutableTypeVariable mutableTypeVariable = type.getTypeVariables().get(0);

		if (mutableTypeVariable.getUpperBounds() != null && mutableTypeVariable.getUpperBounds().size() > 0) {
			return mutableTypeVariable.getUpperBounds().iterator().next();
		}

		if (mutableTypeVariable.getLowerBounds() != null && mutableTypeVariable.getLowerBounds().size() > 0) {
			return mutableTypeVariable.getLowerBounds().iterator().next();
		}

		return null;
	}

	protected void printCopyByLocalConverter(String localConverterName, PathResolver domainPathResolver, DomainType domainMethodReturnType, String dtoField,
			FormattedPrintWriter pw) {
		pw.println("if (" + localConverterName + " != null) {");
		pw.print(TransferObjectElementPrinter.RESULT_NAME + "." + MethodHelper.toSetter(domainPathResolver.getPath()) + "(" + localConverterName + ".fromDto("
				+ TransferObjectElementPrinter.DTO_NAME + "." + MethodHelper.toGetter(dtoField) + ")");
		pw.println(");");
		pw.println("} else {");
		pw.print(TransferObjectElementPrinter.RESULT_NAME + "." + MethodHelper.toSetter(domainPathResolver.getPath()));
		if (domainMethodReturnType.getKind().equals(MutableTypeKind.TYPEVAR)) {
			pw.print("((" + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX + "_" + ((MutableTypeVariable) domainMethodReturnType).getVariable() + ")");
		} else {
			pw.print("((" + domainMethodReturnType + ")");
		}
		pw.print(TransferObjectElementPrinter.DTO_NAME + "." + MethodHelper.toGetter(dtoField));
		pw.println(");");
		pw.println("}");
	}
}