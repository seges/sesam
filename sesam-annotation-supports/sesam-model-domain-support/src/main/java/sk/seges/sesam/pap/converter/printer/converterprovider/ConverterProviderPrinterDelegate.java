package sk.seges.sesam.pap.converter.printer.converterprovider;

import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableExecutableType;
import sk.seges.sesam.core.pap.model.mutable.api.element.MutableVariableElement;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.utils.ProcessorUtils;
import sk.seges.sesam.core.pap.writer.HierarchyPrintWriter;
import sk.seges.sesam.core.pap.writer.LazyPrintWriter;
import sk.seges.sesam.pap.converter.model.HasConstructorParameters;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider.UsageType;
import sk.seges.sesam.shared.model.converter.ConverterProviderContext;

import javax.lang.model.element.Modifier;
import java.io.PrintWriter;

public class ConverterProviderPrinterDelegate {

	protected final ConverterConstructorParametersResolverProvider parametersResolverProvider;

	public ConverterProviderPrinterDelegate(ConverterConstructorParametersResolverProvider parametersResolverProvider) {
		this.parametersResolverProvider = parametersResolverProvider;
	}

	protected Class<? extends ConverterProviderContext> getResultClass() {
		return ConverterProviderContext.class;
	}

	public void initialize(MutableProcessingEnvironment processingEnv, final HasConstructorParameters type, UsageType usageType) {

		ParameterElement[] generatedParameters = type.getConverterParameters(parametersResolverProvider.getParameterResolver(usageType));

		for (ParameterElement generatedParameter : generatedParameters) {
			ProcessorUtils.addField(processingEnv, type, generatedParameter.getType(), generatedParameter.getName());
		}

	}

	public void finish(final HasConstructorParameters type) {
	}
}