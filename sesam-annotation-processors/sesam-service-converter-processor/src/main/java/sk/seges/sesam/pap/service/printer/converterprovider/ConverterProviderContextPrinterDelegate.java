package sk.seges.sesam.pap.service.printer.converterprovider;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;

import sk.seges.sesam.core.pap.model.ConstructorParameter;
import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.ParameterElement.ParameterUsageContext;
import sk.seges.sesam.core.pap.model.ParameterElement.ParameterUsageProvider;
import sk.seges.sesam.core.pap.model.mutable.api.MutableReferenceType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableType;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;
import sk.seges.sesam.pap.converter.printer.converterprovider.ConverterProviderPrinterDelegate;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider.UsageType;
import sk.seges.sesam.pap.model.resolver.api.ConverterConstructorParametersResolver;
import sk.seges.sesam.pap.model.utils.ConstructorHelper;
import sk.seges.sesam.pap.service.model.ConverterProviderContextType;

public class ConverterProviderContextPrinterDelegate {

	private ConverterProviderPrinterDelegate printerDelegate;
	
	private final ConverterConstructorParametersResolverProvider parametersResolverProvider;
	private ConverterProviderContextType converterProviderContextType;
    private final MutableProcessingEnvironment processingEnv;

	public ConverterProviderContextPrinterDelegate(MutableProcessingEnvironment processingEnv, ConverterConstructorParametersResolverProvider parametersResolverProvider) {
		this.parametersResolverProvider = parametersResolverProvider;
        this.processingEnv = processingEnv;
	}
	
	public void finalize() {
        converterProviderContextType = null;
    }
	
	public void initialize(MutableProcessingEnvironment processingEnv, ConverterProviderContextType converterProviderType, UsageType usageType) {
        this.converterProviderContextType = converterProviderType;
		this.printerDelegate = new ConverterProviderPrinterDelegate(parametersResolverProvider);
		this.printerDelegate.initialize(processingEnv, converterProviderType, usageType);
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

    public void print(Element converterPrinterDelegate) {
        HierarchyPrintWriter pw = converterProviderContextType.getConstructor().getPrintWriter();
        pw.print("registerConverterProvider(new ", converterPrinterDelegate, "(");
        //ConverterConstructorParametersResolver parameterResolver = parametersResolverProvider.getParameterResolver(UsageType.CONVERTER_PROVIDER_CONSTRUCTOR);
        List<MutableVariableElement> converterParameters = converterProviderContextType.getConstructor().getParameters();
        //converterProviderContextType.getConverterParameters(parameterResolver);

        List<ExecutableElement> constructors = ElementFilter.constructorsIn(converterPrinterDelegate.getEnclosedElements());

        List<ConstructorParameter> constructorParameters = new ArrayList<ConstructorParameter>();

        if (constructors.size() > 0) {
            constructorParameters = ConstructorHelper.getConstructorParameters(processingEnv.getTypeUtils(), constructors.get(0));
        }

        int j = 0;

        for (final ConstructorParameter constructorParameter: constructorParameters) {
            if (j > 0) {
                pw.print(", ");
            }
            final MutableVariableElement converterParameter = getParameterElementByType(constructorParameter, converterParameters);

            ParameterElement parameterElement = null;

            if (converterParameter == null) {
                parameterElement = new ParameterElement(constructorParameter.getType(), constructorParameter.getName(), new ParameterUsageProvider() {

                    @Override
                    public MutableType getUsage(ParameterUsageContext context) {
                        return constructorParameter.getType();
                    }
                }, true);
                converterProviderContextType.getConstructor().addParameter(processingEnv.getElementUtils().getParameterElement(
                        constructorParameter.getType(), constructorParameter.getName()));
            } else {
                parameterElement = new ParameterElement(converterParameter.asType(), converterParameter.getSimpleName(), new ParameterUsageProvider() {

                    @Override
                    public MutableType getUsage(ParameterUsageContext context) {
                        return converterParameter.asType();
                    }
                }, true);
            }

            MutableType usage = parameterElement.getUsage(new ParameterUsageContext() {

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
}