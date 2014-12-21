package sk.seges.sesam.pap.converter.printer.providercontext;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableExecutableType;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;
import sk.seges.sesam.core.pap.writer.LazyPrintWriter;
import sk.seges.sesam.pap.converter.model.HasConstructorParameters;
import sk.seges.sesam.pap.converter.printer.base.ProviderTypePrinter;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.service.printer.converterprovider.ConverterProviderContextPrinter;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;

import javax.lang.model.element.Modifier;

/**
 * Created by PeterSimun on 21.12.2014.
 */
public abstract class ProviderContextTypePrinter extends ProviderTypePrinter {

    protected static final String GET_METHOD_NAME = "get";

    public ProviderContextTypePrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        super(parametersResolverProvider);
    }

    protected abstract Class<?> getResultClass();

    @Override
    public void initialize(MutableProcessingEnvironment processingEnv, final HasConstructorParameters type, ProviderConstructorParametersResolverProvider.UsageType usageType) {
        MutableExecutableType getMethod =
                processingEnv.getTypeUtils().getExecutable(processingEnv.getTypeUtils().toMutableType(getResultClass()), GET_METHOD_NAME).addModifier(Modifier.PUBLIC);

        type.addMethod(getMethod);
        super.initialize(processingEnv, type, usageType);
        HierarchyPrintWriter printWriter = getPrintWriter(type);
        printWriter.print(type, " " + ConverterProviderContextPrinter.RESULT_INSTANCE_NAME + " = new ", type, "(");
        printWriter.addLazyPrinter(new LazyPrintWriter(processingEnv) {
            @Override
            protected void print() {
                int i = 0;
                for (MutableVariableElement parameter : type.getConstructor().getParameters()) {
                    if (i > 0) {
                        print(", ");
                    }
                    print(parameter.getSimpleName());
                    i++;
                }
            }
        });
        printWriter.println(");");
    }

    @Override
    public void finish(HasConstructorParameters type) {
        getPrintWriter(type).println("return " + ConverterProviderContextPrinter.RESULT_INSTANCE_NAME + ";");
    }

    public static HierarchyPrintWriter getPrintWriter(MutableDeclaredType type) {
        return type.getMethod(GET_METHOD_NAME).getPrintWriter();
    }
}