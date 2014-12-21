package sk.seges.sesam.pap.converter.printer.entityprovider;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.model.model.ConverterTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.entity.EntityTargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PeterSimun on 14.12.2014.
 */
public class AbstractDomainMethodEntityProviderPrinter extends AbstractEntityProviderPrinter {

    protected static final String DOMAIN_CLASS_PARAMETER_NAME = "domainClass";

    private static final String RESULT_NAME = "result";

    protected AbstractDomainMethodEntityProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
        super(processingEnv, pw);
    }

    @Override
    public void initializeProviderMethod() {
        MutableDeclaredType listType = processingEnv.getTypeUtils().toMutableType(List.class).setTypeVariables();
        MutableDeclaredType classType = processingEnv.getTypeUtils().toMutableType(Class.class).setTypeVariables();

        pw.println("public <" + ConverterTypeElement.DTO_TYPE_ARGUMENT_PREFIX + ", " + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX +
                        "> ", listType, "<", classType, "<?>> " + EntityTargetType.DOMAIN.getProviderMethodName() + "(", Class.class.getSimpleName(), "<" + ConverterTypeElement.DOMAIN_TYPE_ARGUMENT_PREFIX + "> ",
                DOMAIN_CLASS_PARAMETER_NAME + ") {");
        pw.println();
        pw.println("if (" + DOMAIN_CLASS_PARAMETER_NAME + " == null) {");
        pw.println("return null;");
        pw.println("}");
        pw.println();
        pw.println(listType, "<", classType, "<?>> " + RESULT_NAME + " = new ", processingEnv.getTypeUtils().toMutableType(ArrayList.class).setTypeVariables(), "<", classType, "<?>>();");
        pw.println();
    }

    @Override
    protected boolean checkContext(AbstractProviderPrinterContext context) {
        return context.getDomain().getKind().isDeclared() && context.getConverterType() != null;
    }

    @Override
    protected MutableDeclaredType getTargetEntity(AbstractProviderPrinterContext context) {
        return context.getConverterType().getConfiguration().getInstantiableDomain();
    }

    @Override
    protected String getParameterName() {
        return DOMAIN_CLASS_PARAMETER_NAME;
    }

    @Override
    protected void printResult(AbstractProviderPrinterContext context) {
        pw.print(RESULT_NAME + ".add(", context.getDto().clone().setTypeVariables(), ".class)");
    }

    @Override
    public void finish() {
        if (types.size() > 0) {
            pw.println("return " + RESULT_NAME + ";");
            pw.println("}");
        }
    }
}