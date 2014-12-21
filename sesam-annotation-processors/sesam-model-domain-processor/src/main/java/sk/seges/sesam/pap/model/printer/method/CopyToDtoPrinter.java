package sk.seges.sesam.pap.model.printer.method;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.model.PathResolver;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror.MutableTypeKind;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.Field;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.ElementHolderTypeConverter;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.model.api.dto.DtoType;
import sk.seges.sesam.pap.model.printer.api.TransferObjectElementPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterTargetType;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;

public class CopyToDtoPrinter extends AbstractMethodPrinter implements TransferObjectElementPrinter {

	protected final FormattedPrintWriter pw;

	protected ElementHolderTypeConverter elementHolderTypeConverter;
	
	public CopyToDtoPrinter(ConverterProviderPrinter converterProviderPrinter, ElementHolderTypeConverter elementHolderTypeConverter, EntityResolver entityResolver, 
			ProviderConstructorParametersResolverProvider parametersResolverProvider, RoundEnvironment roundEnv, TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(converterProviderPrinter, parametersResolverProvider, entityResolver, roundEnv, processingEnv);
		this.pw = pw;
		this.elementHolderTypeConverter = elementHolderTypeConverter;
	}
	
	@Override
	public void print(TransferObjectContext context) {
		copy(context, pw, new CopyToDtoMethodPrinter(converterProviderPrinter, elementHolderTypeConverter, parametersResolverProvider, entityResolver, roundEnv, processingEnv));
	}

	@Override
	public void initialize(ConfigurationTypeElement configurationElement, MutableDeclaredType outputName) {
		
		DtoDeclaredType dtoType = configurationElement.getDto();
		DomainDeclaredType domainType = configurationElement.getDomain();
		
		pw.println("public ", dtoType, " createDtoInstance(", Serializable.class, " id) {");
		printDtoInstancer(pw, entityResolver, dtoType);
		pw.println("}");
		pw.println();
							
		pw.println("public ", dtoType, " toDto(", domainType, " " + DOMAIN_NAME + ") {");
		pw.println();
		pw.println("if (" + DOMAIN_NAME + "  == null) {");
		pw.println("return null;");
		pw.println("}");
		pw.println();

		ExecutableElement idMethod = null;
		
		if (domainType.getKind().isDeclared()) {
			idMethod = configurationElement.getInstantiableDomain().getIdMethod(entityResolver);
			
			if (idMethod == null && entityResolver.shouldHaveIdMethod(configurationElement.getInstantiableDomain())) {
				processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find id method for " + configurationElement.toString(), configurationElement.asConfigurationElement());
				return;
			}
		}
		
		if (idMethod == null) {
			//TODO potential cycle
			pw.println(dtoType, " " + RESULT_NAME + " = createDtoInstance(null);");
		} else {

			boolean useIdConverter = false;

			MutableTypeMirror dtoIdType = processingEnv.getTypeUtils().toMutableType(idMethod.getReturnType());
			DomainType domainIdTypeElement = null;
			
			if (idMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
				domainIdTypeElement = configurationElement.getInstantiableDomain().getId(entityResolver);
				DtoType dto = domainIdTypeElement.getDto();
				if (dto != null) {
					dtoIdType = dto;
				}
			}
							
			pw.println(dtoType, " " + RESULT_NAME + " = getDtoInstance(" + DOMAIN_NAME + ", " + DOMAIN_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(idMethod)) + ");");
			pw.println("if (" + RESULT_NAME + " != null) {");
			pw.println("return " + RESULT_NAME + ";");
			pw.println("}");
			pw.println();
			
			String idName = "_id";
			
			
			String methodName = DOMAIN_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(idMethod));

			if (idMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
				if (domainIdTypeElement.getConverter() != null) {
					pw.print(dtoIdType, " " + idName + " = ");
					Field field = new Field(methodName, processingEnv.getTypeUtils().toMutableType(domainIdTypeElement));
					//converterProviderPrinter.printDomainEnsuredConverterMethodName(domainIdTypeElement, null, field, idMethod, pw, false);
					//TODO add NPE check
					converterProviderPrinter.printObtainConverterFromCache(pw, ConverterTargetType.DOMAIN, domainIdTypeElement, field, idMethod, true);

					pw.print(".convertToDto(");
					//converterProviderPrinter.printDomainEnsuredConverterMethodName(domainIdTypeElement, null, field, idMethod, pw, false);
					//TODO add NPE check
					converterProviderPrinter.printObtainConverterFromCache(pw, ConverterTargetType.DOMAIN, domainIdTypeElement, field, idMethod, true);

					pw.print(".createDtoInstance(null), ");
					pw.println("(", getDelegateCast(idMethod.getReturnType()), ")" + methodName + ");");
					pw.println();
					useIdConverter = true;
				}
			}

			pw.println(RESULT_NAME + " = createDtoInstance(" + (useIdConverter ? idName : methodName) + ");");
		}

		pw.println("return convertToDto(" + RESULT_NAME + ", " + DOMAIN_NAME + ");");
		pw.println("}");
		pw.println();
		
		pw.println("public ", dtoType, " convertToDto(", dtoType, " " + RESULT_NAME + ", ", domainType, " " + DOMAIN_NAME + ") {");
		pw.println();
		pw.println("if (" + DOMAIN_NAME + "  == null) {");
		pw.println("return null;");
		pw.println("}");
		pw.println();

		if (idMethod != null) {
			pw.println(dtoType, " dtoFromCache = getDtoFromCache(" + DOMAIN_NAME + ", " + DOMAIN_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(idMethod)) + ");");
			pw.println();
			pw.println("if (dtoFromCache != null) {");
			pw.println("return dtoFromCache;");
			pw.println("}");
			pw.println();
		}

		DtoDeclaredType dtoSuperClass = configurationElement.getDto().getSuperClass();

		if (dtoSuperClass != null && dtoSuperClass.getConverter() != null) {
			dtoSuperClass = dtoSuperClass.getDomainDefinitionConfiguration().getDto();
		}

		if (dtoSuperClass != null && dtoSuperClass.getConverter() != null && dtoSuperClass.getKind().equals(MutableTypeKind.CLASS)) {
			MutableDeclaredType fieldType = processingEnv.getTypeUtils().getDeclaredType(processingEnv.getTypeUtils().toMutableType(Class.class), new MutableDeclaredType[] { dtoSuperClass.getDomain() });
			//TODO: change canonical name to simple name and add import

			processingEnv.getUsedTypes().add(dtoSuperClass);

			Field field = new Field(dtoSuperClass.getSimpleName() + ".class", fieldType);

			//TODO add NPE check
			converterProviderPrinter.printObtainConverterFromCache(pw, ConverterTargetType.DTO, dtoSuperClass.getDomain(), field, null, false);

			pw.println(".convertToDto(" + RESULT_NAME + ", " + DOMAIN_NAME + ");");
			pw.println();
		}

		if (idMethod != null) {
			pw.println("putDtoIntoCache(" + DOMAIN_NAME + ", " + RESULT_NAME + "," + RESULT_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(idMethod)) + ");");
			pw.println();
		}
	}
	
	@Override
	public void finish(ConfigurationTypeElement configurationTypeElement) {
		pw.println("return " + RESULT_NAME + ";");
		pw.println("}");
		pw.println();
	}
	
	protected void printIsInitializedMethod(PrintWriter pw, ExecutableElement domainMethod, PathResolver domainPathResolver) {
//	protected void printIsInitializedMethod(PrintWriter pw, String instanceName) {
		pw.println("return true;");
	}
}