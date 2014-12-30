package sk.seges.sesam.pap.service.printer;

import sk.seges.sesam.core.pap.accessor.AnnotationAccessor.AnnotationFilter;
import sk.seges.sesam.core.pap.accessor.AnnotationAccessor.AnnotationTypeFilter;
import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.*;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableExecutableElement;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;
import sk.seges.sesam.core.pap.writer.LazyPrintWriter;
import sk.seges.sesam.pap.model.model.Field;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;
import sk.seges.sesam.pap.model.printer.converter.ParameterUsagePrinter;
import sk.seges.sesam.pap.model.resolver.DefaultConverterConstructorParametersResolver;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.service.printer.model.ServiceConverterPrinterContext;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceMethodConverterPrinter extends AbstractServiceMethodPrinter {

	public static final String RESULT_VARIABLE_NAME = "result";

	public ServiceMethodConverterPrinter(TransferObjectProcessingEnvironment processingEnv,
			ProviderConstructorParametersResolverProvider parametersResolverProvider, ConverterProviderPrinter converterProviderPrinter) {
		super(processingEnv, parametersResolverProvider, converterProviderPrinter);
	}

	protected void printCastLocalMethodResult(FormattedPrintWriter pw, MutableTypeMirror returnLocalType, ServiceConverterPrinterContext context) {
		if (returnLocalType == null) {
			return;
		}
		
		switch (returnLocalType.getKind()) {
			case CLASS:
			case INTERFACE:
				if (stripWildcardTypeVariables(returnLocalType)) {
					pw.print("(", ((MutableDeclaredType)returnLocalType).clone().stripTypeParameters(), ")");
				}
				
			default:
		}
	}

	private boolean stripWildcardTypeVariables(MutableTypeVariable typeVariable) {
		if (typeVariable.getVariable() != null && typeVariable.getVariable().equals(MutableWildcardType.WILDCARD_NAME)) {
			return true;
		}
			
		if (typeVariable.getLowerBounds() != null) {
			for (MutableTypeMirror lowerBound: typeVariable.getLowerBounds()) {
				if (stripWildcardTypeVariables(lowerBound)) {
					return true;
				}
			}
		}
		
		if (typeVariable.getUpperBounds() != null) {
			for (MutableTypeMirror upperBound: typeVariable.getUpperBounds()) {
				if (stripWildcardTypeVariables(upperBound)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean stripWildcardTypeVariables(MutableTypeMirror type) {
		if (type == null) {
			return false;
		}
		
		switch (type.getKind()) {
			case CLASS:
			case INTERFACE:
				List<? extends MutableTypeVariable> typeVariables = ((MutableDeclaredType)type).getTypeVariables();
				if (typeVariables != null) {
					for (MutableTypeVariable typeVariable: typeVariables) {
						if (stripWildcardTypeVariables(typeVariable)) {
							return true;
						}
					}
				}
			default:
		}
		
		return false;
	}
	
	private List<AnnotationMirror> getAnnotations(ExecutableElement method, AnnotationTypeFilter... annotationFilters) {
		
		List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();
		
		for (AnnotationMirror annotation: method.getAnnotationMirrors()) {
			
			boolean isAnnotationIgnored = false;
			
			if (annotationFilters != null) {
				for (AnnotationFilter filter: annotationFilters) {
					if (filter.isAnnotationIgnored(annotation)) {
						isAnnotationIgnored = true;
						break;
					}
				}
			}
			
			if (!isAnnotationIgnored) {
				result.add(annotation);
			}
		}
		
		return result;
	}
	
	protected void handleMethod(ServiceConverterPrinterContext context, final ExecutableElement localMethod, ExecutableElement remoteMethod) {

		DtoType returnDtoType = null;
		
		if (!remoteMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
			returnDtoType = processingEnv.getTransferObjectUtils().getDtoType(remoteMethod.getReturnType());
		}

        if (returnDtoType == null) {
            throw new RuntimeException("Unknown return type for the method " + remoteMethod.toString());
        }

		MutableExecutableElement remoteMutableMethod = processingEnv.getElementUtils().toMutableElement(remoteMethod);

		MutableExecutableType mutableMethodType = remoteMutableMethod.asType();
		
		for (AnnotationMirror annotation: getAnnotations(localMethod, new AnnotationTypeFilter(false, getSupportedAnnotations(localMethod)))) {
			mutableMethodType.annotateWith(annotation);
		}
		
		context.getService().getServiceConverter().addMethod(mutableMethodType.addModifier(Modifier.PUBLIC));

		HierarchyPrintWriter pw = remoteMutableMethod.asType().getPrintWriter();
		
		//TODO is NULL ok?

		boolean hasConverter = false;
		
		if (!remoteMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
			if (returnDtoType.getConverter() != null) {
				hasConverter = true;
			}
		}
		
		if (!hasConverter) {
			for (int i = 0; i < localMethod.getParameters().size(); i++) {
				TypeMirror dtoType = remoteMethod.getParameters().get(i).asType();
				DtoType parameterDtoType = processingEnv.getTransferObjectUtils().getDtoType(dtoType);
				
				if (parameterDtoType.getConverter() != null) {
					hasConverter = true;
					break;
				}
			}
		}
	
		if (hasConverter) {

			pw.println(context.getService().getServiceConverter().getField(DefaultConverterConstructorParametersResolver.CONVERTER_PROVIDER_CONTEXT_NAME).asType(),
					" " + DefaultConverterConstructorParametersResolver.CONVERTER_PROVIDER_CONTEXT_NAME +
					" = this.", DefaultConverterConstructorParametersResolver.CONVERTER_PROVIDER_CONTEXT_NAME, ".get();");

			pw.addLazyPrinter(new LazyPrintWriter(processingEnv) {
				
				@Override
				protected void print() {
					converterProviderPrinter.printConverterParams(localMethod, this);
				}
			});

			ParameterElement[] converterParameters = parametersResolverProvider.getParameterResolver(ProviderConstructorParametersResolverProvider.UsageType.DEFINITION).getConstructorAdditionalParameters();

			ParameterUsagePrinter parameterUsagePrinter = new ParameterUsagePrinter(pw);

			for (ParameterElement converterParameter: converterParameters) {
				if (converterParameter.getPropagationType().equals(PropagationType.INSTANTIATED)) {

					MutableType usage = converterParameter.getUsage(new ParameterElement.ParameterUsageContext() {

						@Override
						public ExecutableElement getMethod() {
							return null;
						}
					});

					if (usage instanceof MutableReferenceType && parameterUsagePrinter.isInline(usage)) {
						pw.println(DefaultConverterConstructorParametersResolver.CONVERTER_PROVIDER_CONTEXT_NAME + "." + MethodHelper.toSetter(converterParameter.getName()) + "(new ", parameterUsagePrinter.getReferenceType(usage), "());");
					}  else {
						pw.println(DefaultConverterConstructorParametersResolver.CONVERTER_PROVIDER_CONTEXT_NAME + "." + MethodHelper.toSetter(converterParameter.getName()) + "(" + converterParameter.getName()  + ");");
					}
				}
			}
		}

		MutableTypeMirror returnType = null;
		if (!remoteMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
			pw.print(returnType = ProcessorUtils.stripTypeParametersVariables(processingEnv.getTypeUtils().toMutableType(localMethod.getReturnType())), " " + RESULT_VARIABLE_NAME + " = ");
		}
		
		printCastLocalMethodResult(pw, returnType, context);
		
		pw.print(context.getLocalServiceFieldName() + "." + localMethod.getSimpleName().toString() + "(");

		for (int i = 0; i < localMethod.getParameters().size(); i++) {
			if (i > 0) {
				pw.print(", ");
			}

			TypeMirror dtoType = remoteMethod.getParameters().get(i).asType();
			
			DtoType parameterDtoType = processingEnv.getTransferObjectUtils().getDtoType(dtoType);
			final DomainType parameterDomainType = processingEnv.getTransferObjectUtils().getDomainType(ProcessorUtils.stripTypeParametersVariables(parameterDtoType.getDomain()));
			
			String parameterName = remoteMethod.getParameters().get(i).getSimpleName().toString();

			//remote parameter
			MutableTypeMirror remoteParameterType = findSubtypesForWildcards(
					ProcessorUtils.stripTypeParametersTypes(processingEnv.getTypeUtils().toMutableType(dtoType)), remoteMethod);
			
			//local parameter
			TypeMirror domainType = localMethod.getParameters().get(i).asType();

			MutableTypeMirror localParameterType = findSubtypesForWildcards(
					ProcessorUtils.stripTypeParametersTypes(processingEnv.getTypeUtils().toMutableType(domainType)), localMethod);

			if (parameterDtoType.getConverter() != null || !remoteParameterType.isSameType(localParameterType)) {
				pw.print("(", parameterDomainType, ")");
				
				final Field field = new Field("(" + remoteParameterType.toString(ClassSerializer.SIMPLE, true) + ")" + parameterName, parameterDtoType);
				pw.print("(");

				pw.addLazyPrinter(new LazyPrintWriter(processingEnv) {
					
					@Override
					protected void print() {
						converterProviderPrinter.printObtainConverterFromCache(this, ConverterTargetType.DTO, parameterDomainType, field, localMethod, false);
					}
				});

				//NPE check
				pw.print(" == null ? null : ");
				
				pw.addLazyPrinter(new LazyPrintWriter(processingEnv) {
					
					@Override
					protected void print() {
						converterProviderPrinter.printObtainConverterFromCache(this, ConverterTargetType.DTO, parameterDomainType, field, localMethod, false);
					}
				});
				
				pw.print(".fromDto((", remoteParameterType, ")");
			}

			pw.print(parameterName);

			if (parameterDtoType.getConverter() != null || !remoteParameterType.isSameType(localParameterType)) {
				pw.print("))");
			}
		}

		pw.print(")");
		pw.println(";");

		boolean shouldBeConverted = !remoteMethod.getReturnType().getKind().equals(TypeKind.VOID) && returnDtoType.getConverter() != null;
				
		if (!shouldBeConverted && !remoteMethod.getReturnType().getKind().equals(TypeKind.VOID) && returnDtoType.getConverter() == null) {
			//remote return type
			MutableTypeMirror remoteReturnType = findSubtypesForWildcards(
					ProcessorUtils.stripTypeParametersTypes(processingEnv.getTypeUtils().toMutableType(returnType)), remoteMethod);
			
			//local return type
			MutableTypeMirror localReturnType = findSubtypesForWildcards(
					ProcessorUtils.stripTypeParametersTypes(processingEnv.getTypeUtils().toMutableType(localMethod.getReturnType())), localMethod);
			
			shouldBeConverted = !remoteReturnType.isSameType(localReturnType);
		}


		if (shouldBeConverted) {

			returnType = ProcessorUtils.stripTypeParametersTypes(processingEnv.getTypeUtils().toMutableType(remoteMethod.getReturnType()));
			
			pw.print("return (", returnType, ")");
			
			pw.print("(");
			
			final Field field = new Field(RESULT_VARIABLE_NAME, returnType);
			
			final DtoType selectedReturnDtoType = returnDtoType;
			
			pw.addLazyPrinter(new LazyPrintWriter(processingEnv) {
				
				@Override
				protected void print() {
					converterProviderPrinter.printObtainConverterFromCache(this, ConverterTargetType.DOMAIN, selectedReturnDtoType.getDomain(), field, localMethod, false);
				}
			});
			
			//NPE check
			pw.print(" == null ? null : ");
			pw.addLazyPrinter(new LazyPrintWriter(processingEnv) {
				
				@Override
				protected void print() {
					converterProviderPrinter.printObtainConverterFromCache(this, ConverterTargetType.DOMAIN, selectedReturnDtoType.getDomain(), field, localMethod, false);
				}
			});
			
			pw.println(".toDto(" + RESULT_VARIABLE_NAME + "));");
		} else if (!remoteMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
			pw.println("return " + RESULT_VARIABLE_NAME + ";");
		}
	}

	private MutableTypeMirror getBoundType(MutableTypeVariable typeVariable, ExecutableElement ownerElement) {
		Set<? extends MutableTypeMirror> lowerBounds = typeVariable.getLowerBounds();
		
		if (lowerBounds != null && lowerBounds.size() > 0) {
			return lowerBounds.iterator().next();
		}

		Set<? extends MutableTypeMirror> upperBounds = typeVariable.getUpperBounds();
		
		if (upperBounds != null && upperBounds.size() > 0) {
			return upperBounds.iterator().next();
		}

		List<? extends TypeParameterElement> typeParameters = ownerElement.getTypeParameters();
		for (TypeParameterElement typeParameter: typeParameters) {
			if (typeParameter.getSimpleName().toString().equals(typeVariable.getVariable())) {
				if (typeParameter.getBounds().size() > 0) {
					return processingEnv.getTypeUtils().toMutableType(typeParameter.getBounds().iterator().next());
				}
			}
		}
		
		return null;
	}
	
	private List<MutableTypeVariable> getSubtypesForWildcards(MutableDeclaredType ownerType, List<? extends MutableTypeVariable> typeVariables, ExecutableElement ownerMethod) {
		
		List<MutableTypeVariable> result = new ArrayList<MutableTypeVariable>();

		int i = 0;
		for (MutableTypeVariable typeVariable: typeVariables) {
			if (typeVariable.getVariable() != null && typeVariable.getVariable().equals(MutableWildcardType.WILDCARD_NAME)) {
				Set<? extends MutableTypeMirror> lowerBounds = typeVariable.getLowerBounds();
				Set<? extends MutableTypeMirror> upperBounds = typeVariable.getUpperBounds();
				
				int size = (lowerBounds == null ? 0 : lowerBounds.size()) + (upperBounds == null ? 0 : upperBounds.size());
				
				if (size == 0) {
					//it is only wildcard so try to identify type from owner
					result.add(processingEnv.getTypeUtils().getTypeVariable(null, getBoundType(ownerType.getTypeVariables().get(i), ownerMethod)));
				} else {
					result.add(processingEnv.getTypeUtils().getTypeVariable(null, typeVariable));
				}
			} else {
				result.add(typeVariable);
			}
			
			i++;
		}
		
		return result;
	}
	
	private MutableTypeMirror findSubtypesForWildcards(MutableTypeMirror type, ExecutableElement ownerMethod) {
		if (type == null) {
			return null;
		}
		
		switch (type.getKind()) {
			case CLASS:
			case INTERFACE:
				MutableDeclaredType clone = ((MutableDeclaredType)type).clone();
				List<? extends MutableTypeVariable> typeVariables = clone.getTypeVariables();
				clone.setTypeVariables(getSubtypesForWildcards(processingEnv.getTypeUtils().toMutableType(processingEnv.getElementUtils().getTypeElement(((MutableDeclaredType)type).getCanonicalName())),
						typeVariables, ownerMethod).toArray(new MutableTypeVariable[]{}));
				return clone;
			case TYPEVAR:
				if (((MutableTypeVariable)type).getVariable() != null && ((MutableTypeVariable)type).getVariable().equals(MutableWildcardType.WILDCARD_NAME)) {
					return getBoundType(((MutableTypeVariable)type).clone(), ownerMethod);
				}
			default:
				return type;
		}
	}

	protected Class<?>[] getSupportedAnnotations(Element method) {
		return new Class<?>[] {
			Override.class
		};
	}
}