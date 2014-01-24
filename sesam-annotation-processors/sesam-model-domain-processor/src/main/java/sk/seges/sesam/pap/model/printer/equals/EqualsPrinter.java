package sk.seges.sesam.pap.model.printer.equals;

import sk.seges.sesam.core.pap.model.api.ClassSerializer;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.accessor.GenerateEqualsAccessor;
import sk.seges.sesam.pap.model.annotation.TraversalType;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.printer.AbstractElementPrinter;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;

import javax.tools.Diagnostic.Kind;
import java.util.Arrays;

/**
 * {@link EqualsPrinter} prints equals method based on the fields defined in the class.
 * Equals method tries to detect endless loops by checking whether the equals method isn't 
 * already processed by setting a <i>processingEquals</i> flag. When infinite loops is detected
 * no referenced entities are processed for equals purposes.
 * <pre>
 * This is used when entity A has reference to the entity B and entity B has reference to the entity A.
 * Then infinite loops can occur in the equals method.
 * </pre>
 * @author Peter Simun
 *
 */
public class EqualsPrinter extends AbstractElementPrinter {

	private final MutableProcessingEnvironment processingEnv;
	private final EntityResolver entityResolver;
	
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
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputName) {

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

		//TODO we should call also super.equals - but only when there is any superclass with implemented equals method

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
				pw.println("if (" + context.getDtoFieldName() + " == other." + context.getDtoFieldName() + ")");
				pw.println("	return true;");
			} else {
				pw.println("if (" + context.getDtoFieldName() + " != other." + context.getDtoFieldName() + ")");
				pw.println("	return false;");
			}
			return;
		case CLASS:
		case INTERFACE:
			if (idMethod) {
				pw.println("if (" + context.getDtoFieldName() + " != null && other." + context.getDtoFieldName() + " != null && " + context.getDtoFieldName() + ".equals(other." + context.getDtoFieldName() + "))");
				pw.println("	return true;");
			} else {
				pw.println("if (" + context.getDtoFieldName() + " == null) {");
				pw.println("if (other." + context.getDtoFieldName() + " != null)");
				pw.println("	return false;");
				pw.println("} else { ");
				if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
					pw.println("if (!processingEquals) {");
					pw.println("processingEquals = true;");
				}
				pw.println("if (!" + context.getDtoFieldName() + ".equals(other." + context.getDtoFieldName() + ")) {");
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
				pw.println("}");
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
			pw.println("return false");
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
}