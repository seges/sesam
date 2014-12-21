package sk.seges.sesam.pap.provider.printer;

import sk.seges.sesam.core.pap.model.ConstructorParameter;
import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableReferenceType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableType;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;
import sk.seges.sesam.pap.converter.model.AbstractProviderContextType;
import sk.seges.sesam.pap.converter.printer.base.ProviderTypePrinter;
import sk.seges.sesam.pap.converter.printer.converterprovider.ConverterProviderContextTypePrinter;
import sk.seges.sesam.pap.model.resolver.ProviderConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.utils.ConstructorHelper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PeterSimun on 20.12.2014.
 */
public abstract class AbstractProviderContextPrinter {

    protected ProviderTypePrinter providerPrinter;

    protected final ProviderConstructorParametersResolverProvider parametersResolverProvider;
    protected AbstractProviderContextType providerContextType;
    protected final MutableProcessingEnvironment processingEnv;

    public static final String RESULT_INSTANCE_NAME = "result";

    public AbstractProviderContextPrinter(MutableProcessingEnvironment processingEnv, ProviderConstructorParametersResolverProvider parametersResolverProvider) {
        this.parametersResolverProvider = parametersResolverProvider;
        this.processingEnv = processingEnv;
    }

    public void finalize(AbstractProviderContextType converterProviderType) {
        this.providerPrinter.finish(converterProviderType);
        providerContextType = null;
    }

    public void initialize(MutableProcessingEnvironment processingEnv, AbstractProviderContextType converterProviderType, ProviderConstructorParametersResolverProvider.UsageType usageType) {
        this.providerContextType = converterProviderType;
        this.providerPrinter = getProviderTypePrinter(parametersResolverProvider);
        this.providerPrinter.initialize(processingEnv, converterProviderType, usageType);
    }

    protected abstract String getRegisterMethodName();
    protected abstract ProviderTypePrinter getProviderTypePrinter(ProviderConstructorParametersResolverProvider parametersResolverProvider);

    public void print(Element converterPrinterType) {
        HierarchyPrintWriter pw = ConverterProviderContextTypePrinter.getPrintWriter(providerContextType);
        pw.print(RESULT_INSTANCE_NAME + "." + getRegisterMethodName() + "(new ", converterPrinterType, "(");
        List<MutableVariableElement> converterParameters = providerContextType.getConstructor().getParameters();

        List<ExecutableElement> constructors = ElementFilter.constructorsIn(converterPrinterType.getEnclosedElements());

        List<ConstructorParameter> constructorParameters = new ArrayList<ConstructorParameter>();

        if (constructors.size() > 0) {
            constructorParameters = ConstructorHelper.getConstructorParameters(processingEnv.getTypeUtils(), constructors.get(0));
        }

        int j = 0;

        ParameterElement[] constructorAditionalParameters = parametersResolverProvider.getParameterResolver(ProviderConstructorParametersResolverProvider.UsageType.PROVIDER_CONTEXT_CONSTRUCTOR).getConstructorAditionalParameters();

        for (final ConstructorParameter constructorParameter: constructorParameters) {
            if (j > 0) {
                pw.print(", ");
            }
            final MutableVariableElement converterParameter = getParameterElementByType(constructorParameter, converterParameters);

            ParameterElement parameterElement = null;

            for (ParameterElement parameter: constructorAditionalParameters) {
                if (processingEnv.getTypeUtils().isAssignable(parameter.getType(), constructorParameter.getType())) {
                    parameterElement = parameter;
                    if (parameter.getPropagationType().equals(PropagationType.PROPAGATED_IMUTABLE)) {
                        if (!ProcessorUtils.hasField(processingEnv, providerContextType, constructorParameter.getType(), constructorParameter.getName())) {
                            ProcessorUtils.addField(processingEnv, providerContextType, constructorParameter.getType(), constructorParameter.getName());
                        }
                    }
                    break;
                }
            }

            if (parameterElement == null) {
                if (converterParameter == null) {

                    parameterElement = new ParameterElement(constructorParameter.getType(), constructorParameter.getName(), new ParameterElement.ParameterUsageProvider() {

                        @Override
                        public MutableType getUsage(ParameterElement.ParameterUsageContext context) {
                            return constructorParameter.getType();
                        }
                    }, PropagationType.PROPAGATED_IMUTABLE);
                    if (!ProcessorUtils.hasField(processingEnv, providerContextType, constructorParameter.getType(), constructorParameter.getName())) {
                        ProcessorUtils.addField(processingEnv, providerContextType, constructorParameter.getType(), constructorParameter.getName());
                    }
                } else {
                    parameterElement = new ParameterElement(converterParameter.asType(), converterParameter.getSimpleName(), new ParameterElement.ParameterUsageProvider() {

                        @Override
                        public MutableType getUsage(ParameterElement.ParameterUsageContext context) {
                            return converterParameter.asType();
                        }
                    }, PropagationType.PROPAGATED_IMUTABLE);
                }
            }

            MutableType usage = parameterElement.getUsage(new ParameterElement.ParameterUsageContext() {

                @Override
                public ExecutableElement getMethod() {
                    return null;
                }
            });

            if (usage instanceof MutableReferenceType) {
                pw.print(usage);
            } else {
                pw.print(parameterElement.getName());
            }

            j++;
        }
        pw.println("));");
    }

    protected MutableVariableElement getParameterElementByType(ConstructorParameter constructorParameter, List<MutableVariableElement> converterParameters) {
        if (converterParameters == null) {
            return null;
        }

        for (int i = 0; i < converterParameters.size(); i++) {
            if (constructorParameter.getType().isSameType(converterParameters.get(i).asType())) {
                return converterParameters.get(i);
            }
        }

        return null;
    }
}