package sk.seges.sesam.pap.model.printer.hashcode;

import java.util.Arrays;

import javax.tools.Diagnostic.Kind;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.accessor.GenerateHashcodeAccessor;
import sk.seges.sesam.pap.model.annotation.TraversalType;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
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
	public void initialize(ConfigurationTypeElement configurationTypeElement, MutableDeclaredType outputName) {

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
		pw.println("int result = 1;");

		//TODO we should call also super.hashcode - but only when there is any superclass with implemented hashcode method

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
			if (context.getDtoFieldType().toString().equals(boolean.class.getSimpleName())) {
				pw.println("result = prime * result + (((Boolean)" + context.getDtoFieldName() + ").hashCode());");
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
			if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
				pw.println("if (!processingHashCode) {");
				pw.println("processingHashCode = true;");
			}
			pw.println("result = prime * result + ((" + context.getDtoFieldName() + " == null) ? 0 : " + context.getDtoFieldName() + ".hashCode());");
			if (equalsType.equals(TraversalType.CYCLIC_SAFE)) {
				pw.println("processingHashCode = false;");
				pw.println("}");
			}
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
}