package sk.seges.sesam.pap.model.printer.method;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableExecutableElement;
import sk.seges.sesam.core.pap.utils.MethodHelper;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.Field;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.model.api.domain.DomainType;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;
import sk.seges.sesam.pap.model.printer.api.TransferObjectElementPrinter;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;

public class CopyFromDtoPrinter extends AbstractMethodPrinter implements TransferObjectElementPrinter {

	protected final FormattedPrintWriter pw;
	protected final Set<String> nestedInstances;
	
	public CopyFromDtoPrinter(Set<String> nestedInstances, ConverterProviderPrinter converterProviderPrinter, EntityResolver entityResolver, 
			ConverterConstructorParametersResolverProvider parametersResolverProvider, RoundEnvironment roundEnv, TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(converterProviderPrinter, parametersResolverProvider, entityResolver, roundEnv, processingEnv);
		this.nestedInstances = nestedInstances;
		this.pw = pw;
	}
	
	@Override
	public void print(TransferObjectContext context) {
		copy(context, pw, new CopyFromDtoMethodPrinter(nestedInstances, converterProviderPrinter, entityResolver, parametersResolverProvider, roundEnv, processingEnv));
	}

	@Override
	public void initialize(ConfigurationTypeElement configurationElement, MutableDeclaredType outputName) {
		
		DtoDeclaredType dtoType = configurationElement.getDto();
		DomainDeclaredType domainType = configurationElement.getDomain();
		
		ExecutableElement domainIdMethod = null;
		
		if (domainType.getKind().isDeclared()) {
			domainIdMethod = configurationElement.getInstantiableDomain().getIdMethod(entityResolver);
			
			if (domainIdMethod == null && entityResolver.shouldHaveIdMethod(configurationElement.getInstantiableDomain())) {
				processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find id method for " + configurationElement.toString(), configurationElement.asConfigurationElement());
				return;
			}
		}
		
		pw.println("public ", domainType, " createDomainInstance(", Serializable.class, " id) {");
		
		printDomainInstancer(pw, configurationElement.getInstantiableDomain());
		pw.println("}");
		pw.println();

		pw.println("public ", domainType, " fromDto(", dtoType, " " + DTO_NAME + ") {");
		pw.println();
		pw.println("if (" + DTO_NAME + " == null) {");
		pw.println("return null;");
		pw.println("}");
		pw.println();
		
		MutableExecutableElement dtoIdMethod = configurationElement.getDto().getIdMethod(entityResolver);
		
		if (dtoIdMethod == null && entityResolver.shouldHaveIdMethod(configurationElement.getInstantiableDomain())) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "[ERROR] Unable to find id method for DTO class " + dtoType.getCanonicalName(), configurationElement.asConfigurationElement());
			return;
		}
		
		if (dtoIdMethod == null) {
			//TODO potential cycle
			pw.println(domainType, " " + RESULT_NAME + " = createDomainInstance(null);");
		} else {

			boolean useIdConverter = false;

			MutableTypeMirror dtoIdType = dtoIdMethod.asType().getReturnType();
			DomainType domainIdType = processingEnv.getTransferObjectUtils().getDtoType(dtoIdType).getDomain();
			
			pw.println(domainType, " " + RESULT_NAME + " = getDomainInstance(" + DTO_NAME + ", " + DTO_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(dtoIdMethod)) + ");");
			pw.println("if (" + RESULT_NAME + " != null) {");
			pw.println("return " + RESULT_NAME + ";");
			pw.println("}");

			String idName = "_id";
			
			if (domainIdType != null) {
				pw.print(domainIdType, " " + idName + " = ");
			} else {
				//Types are the same
				pw.print(dtoIdType, " " + idName + " = ");
			}
			
			if (domainIdMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
				ConverterTypeElement idConverter = domainIdType.getConverter();
					//toHelper.getConfigurationElement(domainIdType, roundEnv);
				if (idConverter != null) {
					Field field = new Field(DTO_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(dtoIdMethod)), domainIdType.getDto());
					converterProviderPrinter.printDtoEnsuredConverterMethodName(domainIdType.getDto(), field, domainIdMethod, pw, false);
					pw.print(".fromDto(");
					useIdConverter = true;
				}
			}

			pw.print(DTO_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(dtoIdMethod)));

			if (useIdConverter) {
				pw.print(")");
			}
			pw.println(";");
			pw.println();

			pw.println(RESULT_NAME + " = createDomainInstance(" + idName + ");");
		}
		
		pw.println();
		pw.println("return convertFromDto(" + RESULT_NAME + ", " + DTO_NAME + ");");
		pw.println("}");
		pw.println();
		pw.println("public ", domainType, " convertFromDto(", domainType, " " + RESULT_NAME + ", ", dtoType, " " + DTO_NAME + ") {");
		pw.println();
		pw.println("if (" + DTO_NAME + "  == null) {");
		pw.println("return null;");
		pw.println("}");
		pw.println();

		if (dtoIdMethod != null) {
			pw.println(domainType, " domainFromCache = getDomainFromCache(" + DTO_NAME + ", " + DTO_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(dtoIdMethod)) + ");");
			pw.println();
			pw.println("if (domainFromCache != null) {");
			pw.println("return domainFromCache;");
			pw.println("}");
			pw.println();
		}
		
		DomainDeclaredType domainsuperClass = configurationElement.getDomain().getSuperClass();
		
		if (domainsuperClass != null && domainsuperClass.getConverter() != null) {
			MutableDeclaredType fieldType = processingEnv.getTypeUtils().getDeclaredType(processingEnv.getTypeUtils().toMutableType(Class.class), 
					new MutableDeclaredType[] { domainsuperClass.getDto() });
			Field field = new Field(domainsuperClass.getDto().getSimpleName() + ".class", fieldType);
			converterProviderPrinter.printDtoEnsuredConverterMethodName(domainsuperClass.getDto(), field, null, pw, false);
			pw.println(".convertFromDto(" + RESULT_NAME + ", " + DTO_NAME + ");");
			pw.println();
		}

		if (dtoIdMethod != null) {
			pw.println("putDomainIntoCache(" + DTO_NAME + ", " + RESULT_NAME + "," + RESULT_NAME + "." + MethodHelper.toGetter(MethodHelper.toField(domainIdMethod)) + ");");
			pw.println();
		}
	}
	
	@Override
	public void finish(ConfigurationTypeElement configuratioTypeElement) {
		pw.println("return " + RESULT_NAME + ";");
		pw.println("}");
		pw.println();
	}

	protected void printDomainInstancer(FormattedPrintWriter pw, DomainDeclaredType domainTypeElement) {
		if (!entityResolver.shouldHaveIdMethod(domainTypeElement)) {
			pw.println(" return new ", domainTypeElement, "();");
		} else {
			pw.println(domainTypeElement, " " + RESULT_NAME, " = new ", domainTypeElement, "();");
			pw.println(RESULT_NAME + "." + MethodHelper.toSetter(domainTypeElement.getIdMethod(entityResolver)) + "((", domainTypeElement.getId(entityResolver), ")" + "id);");
			pw.println("return " + RESULT_NAME + ";");
		}
	}
}