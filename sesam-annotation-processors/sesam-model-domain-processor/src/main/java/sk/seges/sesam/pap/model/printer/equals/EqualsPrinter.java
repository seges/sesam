package sk.seges.sesam.pap.model.printer.equals;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableExecutableType;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableExecutableElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;
import sk.seges.sesam.pap.model.accessor.GenerateEqualsAccessor;
import sk.seges.sesam.pap.model.annotation.TraversalType;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.api.domain.DomainDeclaredType;
import sk.seges.sesam.pap.model.printer.AbstractElementPrinter;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;
import java.util.Arrays;

/**
 * {@link EqualsPrinter} prints equals method based on the fields defined in the class.
 * Equals method tries to detect endless loops by checking whether the equals method isn't 
 * already processed by setting a <i>processingEquals</i> flag. When infinite loops is detected
 * no referenced entities are processed for equals purposes.
 * <pre>
 * This is used when entityprovider A has reference to the entityprovider B and entityprovider B has reference to the entityprovider A.
 * Then infinite loops can occur in the equals method.
 * </pre>
 * @author Peter Simun
 *
 */
public class EqualsPrinter extends AbstractElementPrinter {

	private final MutableProcessingEnvironment processingEnv;
	private final EntityResolver entityResolver;

	private MutableDeclaredType outputType;
	private boolean supportMethodsPrinted = false;

	private boolean active = true;
	private TraversalType equalsType;
	private boolean hasKey = false;
	
	public EqualsPrinter(EntityResolver entityResolver, MutableProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(pw);
		this.processingEnv = processingEnv;
		this.entityResolver = entityResolver;
	}

	/**
	 * Prints the definition of the equals method with the initial pre-check
	 */
	@Override
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputType) {

		this.outputType = outputType;

		GenerateEqualsAccessor generateEqualsAccessor = new GenerateEqualsAccessor(configurationTypeElement.asConfigurationElement(), processingEnv);

		active = generateEqualsAccessor.generate();
		equalsType = generateEqualsAccessor.getType();

		hasKey = configurationTypeElement.hasKey();

		if (!active) {
			return;
		}

		if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
			pw.println("private boolean processingEquals = false;");
		}

		pw.println("");
		pw.println("@Override");
		pw.println("public boolean equals(Object obj) {");
		pw.println("if (this == obj)");
		pw.println("	return true;");
		pw.println("if (obj == null)");
		pw.println("	return false;");
		pw.println("if (getClass() != obj.getClass())");
		pw.println("	return false;");

		DomainDeclaredType superClass = configurationTypeElement.getInstantiableDomain().getSuperClass();
		if (superClass != null && superClass.getDomainDefinitionConfiguration() != null &&
				new GenerateEqualsAccessor(superClass.getDomainDefinitionConfiguration().asConfigurationElement(), processingEnv).generate()) {
			pw.println("if (!super.equals(obj))");
			pw.println("	return false;");
		}

		MutableDeclaredType targetClassName = getTargetClassNames(configurationTypeElement)[0];
		
		pw.println(targetClassName.toString(ClassSerializer.SIMPLE, true) + " other = (" + 
				targetClassName.toString(ClassSerializer.SIMPLE, true) + ") obj;");
	}

	/**
	 * Enclose equals method
	 */
	@Override
	public void finish(ConfigurationTypeElement configurationTypeElement) {

		if (!active) {
			return;
		}

		pw.println("return true;");
		pw.println("}");
		pw.println();
	}
	
	protected MutableDeclaredType[] getTargetClassNames(ConfigurationTypeElement configurationTypeElement) {

		return new MutableDeclaredType[] {
				configurationTypeElement.getDto()
		};
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

		boolean idMethod = entityResolver.isIdMethod(context.getDtoMethod());
		
		if (idMethod) {
			//TODO That's not really true
			//id's are not interesting
			return;
		}
		
		switch (context.getDtoFieldType().getKind()) {
		case ENUM:
		case PRIMITIVE:
			if (idMethod) {
				pw.println("if (" + context.getDtoFieldName() + " == other." + context.getDtoFieldName() + ") return true;");
			} else {
				pw.println("if (" + context.getDtoFieldName() + " != other." + context.getDtoFieldName() + ") return false;");
			}
			return;
		case CLASS:
		case INTERFACE:
			if (idMethod) {
				pw.println("if (" + context.getDtoFieldName() + " != null && other." + context.getDtoFieldName() + " != null && " + context.getDtoFieldName() + ".equals(other." + context.getDtoFieldName() + "))");
				pw.println("	return true;");
			} else {
				printEqualsSupportMethod();
				pw.println("if (!_equalsSupport(" + context.getDtoFieldName() + ", other." + context.getDtoFieldName() + ")) return false;");
			}
			return;
		case ARRAY:
			if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
				pw.println("if (!processingEquals) {");
				pw.println("processingEquals = true;");
			}
			pw.println("if (!" + Arrays.class.getCanonicalName() + ".equals(" + context.getDtoFieldName() + ", other." + context.getDtoFieldName() + ")) {");
			if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
				pw.println("processingEquals = false;");
			}
			pw.println("return false;");
			if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
				pw.println("} else {");
				pw.println("processingEquals = false;");
				pw.println("}");
			}
			pw.println("}");
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

		if (outputType.getMethod("_equalsSupport") != null) {
			return;
		}

		MutableExecutableType equalsSupportMethod = processingEnv.getTypeUtils().getExecutable(
				processingEnv.getTypeUtils().toMutableType(processingEnv.getTypeUtils().getPrimitiveType(TypeKind.BOOLEAN)), "_equalsSupport").
					addParameter(processingEnv.getElementUtils().getParameterElement(Object.class, "o1")).
					addParameter(processingEnv.getElementUtils().getParameterElement(Object.class, "o2")).addModifier(Modifier.PRIVATE);

		outputType.addMethod(equalsSupportMethod);
		HierarchyPrintWriter printWriter = equalsSupportMethod.getPrintWriter();

		printWriter.println("if (o1 == null) {");
		printWriter.println("if (o2 != null) return false;");
		if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
			printWriter.println("} else if (!processingEquals) {");
			printWriter.println("processingEquals = true;");
			printWriter.println("if (!o1.equals(o2)) return processingEquals = false;");
			printWriter.println("else processingEquals = false;");
			printWriter.println("}");
		} else {
			printWriter.println("} else if (!o1.equals(o2)) return false;");
		}
		printWriter.println("return true;");
	}
}