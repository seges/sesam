package sk.seges.sesam.pap.service.printer.converterprovider;

import sk.seges.sesam.core.pap.model.ConstructorParameter;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.converter.printer.converterprovider.ConverterProviderPrinterDelegate;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider.UsageType;
import sk.seges.sesam.pap.service.model.ConverterProviderContextType;

import javax.lang.model.element.Element;
import java.util.List;

public class ConverterProviderContextPrinterDelegate {

	protected ConverterProviderPrinterDelegate printerDelegate;
	
	protected final ConverterConstructorParametersResolverProvider parametersResolverProvider;
	protected ConverterProviderContextType converterProviderContextType;
    protected final MutableProcessingEnvironment processingEnv;

	public static final String RESULT_INSTANCE_NAME = "result";

	public ConverterProviderContextPrinterDelegate(MutableProcessingEnvironment processingEnv, ConverterConstructorParametersResolverProvider parametersResolverProvider) {
		this.parametersResolverProvider = parametersResolverProvider;
        this.processingEnv = processingEnv;
	}
	
	public void finalize(ConverterProviderContextType converterProviderType) {
		this.printerDelegate.finish(converterProviderType);
        converterProviderContextType = null;
    }
	
	public void initialize(MutableProcessingEnvironment processingEnv, ConverterProviderContextType converterProviderType, UsageType usageType) {
        this.converterProviderContextType = converterProviderType;
		this.printerDelegate = new ConverterProviderPrinterDelegate(parametersResolverProvider);
		this.printerDelegate.initialize(processingEnv, converterProviderType, usageType);
	}

	public void print(Element converterPrinterDelegate) {
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