package sk.seges.sesam.pap.model.printer.hashcode;

import java.util.Arrays;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableExecutableType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;
import sk.seges.sesam.pap.model.accessor.GenerateEqualsAccessor;
import sk.seges.sesam.pap.model.accessor.GenerateHashcodeAccessor;
import sk.seges.sesam.pap.model.annotation.TraversalType;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.printer.AbstractElementPrinter;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;

/**
 * {@link HashCodePrinter} prints hashcode method based on the fields defined in the class.
 * Hashcode method tries to detect endless loops by checking whether the hashCode method isn't 
 * already processed by setting a <i>processingEquals</i> flag. When infinite loops is detected
 * no referenced entities are processed for hashCode purposes.
 * <pre>
 * This is used when entity A has reference to the entity B and entity B has reference to the entity A.
 * Then infinite loops can occur in the hashCode method.
 * </pre>
 * @author Peter Simun
 *
 */
public class HashCodePrinter extends AbstractElementPrinter {

	private MutableProcessingEnvironment processingEnv;
	private EntityResolver entityResolver;

	private MutableDeclaredType outputType;
	private boolean supportMethodsPrinted = false;

	private boolean active = true;
	private TraversalType equalsType;
	private boolean hasKey = false;

	public HashCodePrinter(EntityResolver entityResolver, MutableProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(pw);
		this.processingEnv = processingEnv;
		this.entityResolver = entityResolver;
	}

	/**
	 * Prints the definition of the hashCode method with the initial prechecks
	 */
	@Override
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputType) {

		this.outputType = outputType;

		GenerateHashcodeAccessor generateHashcodeAccessor = new GenerateHashcodeAccessor(configurationTypeElement.asConfigurationElement(), processingEnv);

		active = generateHashcodeAccessor.generate();
		equalsType = generateHashcodeAccessor.getType();

		hasKey = configurationTypeElement.hasKey();

		if (!active) {
			return;
		}

		if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
			pw.println("private boolean processingHashCode = false;");
		}
		pw.println("");
		pw.println("@Override");
		pw.println("public int hashCode() {");
		pw.println("final int prime = 31;");

		//TODO we should call also super.hashcode - but only when there is any superclass with implemented hashcode method
		DomainDeclaredType superClass = configurationTypeElement.getInstantiableDomain().getSuperClass();
		if (superClass != null && superClass.getDomainDefinitionConfiguration() != null &&
				new GenerateEqualsAccessor(superClass.getDomainDefinitionConfiguration().asConfigurationElement(), processingEnv).generate()) {
			pw.println("int result = super.hashCode();");
		} else {
			pw.println("int result = 1;");
		}
	}

	/**
	 * Enclose hashCode method
	 */
	@Override
	public void finish(ConfigurationTypeElement configurationTypeElement) {
		
		if (!active) {
			return;
		}

		pw.println("return result;");
		pw.println("}");
	}
	
	/**
	 * Is executed for every field in the class and prints the logic based on the type (primitive types, declared types, etc.).
	 */
	@Override
	public void print(TransferObjectContext context) {

		if (!active) {
			return;
		}

		if (context.isSuperclassMethod()) {
			return;
		}

		if (hasKey && !context.hasKey()) {
			return;
		}

		if (entityResolver.isIdMethod(context.getDtoMethod())) {
			//TODO Not true
			//IDs are not part of the hashCode
			return;
		}

		switch (context.getDtoFieldType().getKind()) {
		case PRIMITIVE:
			String className = context.getDtoFieldType().toString();
			if (className.equals(boolean.class.getSimpleName())) {
				pw.println("result = prime * result + (((Boolean)" + context.getDtoFieldName() + ").hashCode());");
				return;
			} else if (className.equals(long.class.getSimpleName())) {
				//(int) (contentId ^ (contentId >>> 32))
				pw.println("result = prime * result + ((int)(" + context.getDtoFieldName() + ">>>32));");
				return;
			}
			pw.println("result = prime * result + " + context.getDtoFieldName() + ";");
			return;
		case ENUM:
			pw.println("result = prime * result + ((" + context.getDtoFieldName() + " == null) ? 0 : " + context.getDtoFieldName() + ".hashCode());");
			return;
		case CLASS:
		case INTERFACE:
		case ANNOTATION_TYPE:
			printEqualsSupportMethod();
			pw.println("result = _hashCodeSupport(" + context.getDtoFieldName() + ", prime, result);");
			return;
		case ARRAY:
			if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
				pw.println("if (!processingHashCode) {");
				pw.println("processingHashCode = true;");
			}
			pw.println("result = prime * result + ", Arrays.class, ".hashCode(" + context.getDtoFieldName() + ");");
			if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
				pw.println("processingHashCode = false;");
				pw.println("}");
			}
			return;
		case WILDCARD:
		case VOID:
		case TYPEVAR:
			processingEnv.getMessager().printMessage(Kind.WARNING, "[WARNING] Unsupported type " + context.getDtoFieldName() + " (" + context.getDtoFieldType().getKind() + ") in the " + 
					context.getConfigurationTypeElement().asConfigurationElement(), context.getConfigurationTypeElement().asConfigurationElement());
			return;
		}
	}

	private void printEqualsSupportMethod() {

		if (supportMethodsPrinted) {
			return;
		}

		supportMethodsPrinted = true;

		if (outputType.getMethod("_hashCodeSupport") != null) {
			return;
		}

		MutableExecutableType hashCodeSupportMethod = processingEnv.getTypeUtils().getExecutable(
				processingEnv.getTypeUtils().toMutableType(processingEnv.getTypeUtils().getPrimitiveType(TypeKind.INT)), "_hashCodeSupport").
				addParameter(processingEnv.getElementUtils().getParameterElement(Object.class, "o1")).
				addParameter(processingEnv.getElementUtils().getParameterElement(
						processingEnv.getTypeUtils().toMutableType(processingEnv.getTypeUtils().getPrimitiveType(TypeKind.INT)), "prime")).
				addParameter(processingEnv.getElementUtils().getParameterElement(
						processingEnv.getTypeUtils().toMutableType(processingEnv.getTypeUtils().getPrimitiveType(TypeKind.INT)), "result")).
				addModifier(Modifier.PRIVATE);

		outputType.addMethod(hashCodeSupportMethod);
		HierarchyPrintWriter printWriter = hashCodeSupportMethod.getPrintWriter();

		if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
			printWriter.println("if (!processingHashCode) {");
			printWriter.println("processingHashCode = true;");
			printWriter.println("result = ");
		} else {
			printWriter.println("return ");
		}
		printWriter.print("prime * result + ((o1 == null) ? 0 : o1.hashCode());");
		if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
			printWriter.println("processingHashCode = false;");
			printWriter.println("}");
			printWriter.println("return result;");
		}
	}

}