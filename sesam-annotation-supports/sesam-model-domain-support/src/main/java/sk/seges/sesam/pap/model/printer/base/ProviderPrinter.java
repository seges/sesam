package sk.seges.sesam.pap.model.printer.base;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeVariable;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.converter.printer.api.ProviderElementPrinter;
import sk.seges.sesam.pap.converter.printer.model.AbstractProviderPrinterContext;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by PeterSimun on 14.12.2014.
 */
public abstract class ProviderPrinter implements ProviderElementPrinter {

    protected Set<String> types = new HashSet<String>();

    protected final FormattedPrintWriter pw;
    protected final TransferObjectProcessingEnvironment processingEnv;

    protected ProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
        this.processingEnv = processingEnv;
        this.pw = pw;
    }

    public void initialize() {
        this.types.clear();
    }

    public void finish() {
        if (types.size() > 0) {
            pw.println("return null;");
            pw.println("}");
            pw.println();
        }
    }

    protected abstract boolean checkContext(AbstractProviderPrinterContext context);
    protected abstract MutableDeclaredType getTargetEntity(AbstractProviderPrinterContext context);
    protected abstract void initializeProviderMethod();
    protected abstract String getParameterName();

    @Override
    public void print(AbstractProviderPrinterContext context) {

        if (!checkContext(context) || context.getConverterType() == null) {
            return;
        }

        MutableDeclaredType entity = getTargetEntity(context);

        if (!types.contains(entity.getCanonicalName())) {

            if (types.size() == 0) {
                initializeProviderMethod();
            }

            types.add(entity.getCanonicalName());

            pw.print("if (", entity.clone().setTypeVariables(new MutableTypeVariable[] {}), ".class.");
            pw.print(getClassAssignmentOperator(context));
            pw.println("(" + getParameterName() + ")) {");

            printResult(context);

            pw.println(";");
            pw.println("}");
            pw.println();
        }
    }

    protected abstract void printResult(AbstractProviderPrinterContext context);

    protected String getClassAssignmentOperator(AbstractProviderPrinterContext context) {
        if (context.getConverterType().getConfiguration().hasDtoInterfaceSpecified()) {
            return "isAssignableFrom";
        }

        return "equals";
    }

}