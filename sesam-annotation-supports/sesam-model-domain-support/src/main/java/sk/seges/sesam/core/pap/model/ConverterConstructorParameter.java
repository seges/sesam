package sk.seges.sesam.core.pap.model;

import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.ParameterElement.ParameterUsageContext;
import sk.seges.sesam.core.pap.model.ParameterElement.ParameterUsageProvider;
import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;

public class ConverterConstructorParameter extends ConstructorParameter {

	private PropagationType propagationType;
	
	private ConverterConstructorParameter sameParameter;
	
	private MutableType usage; //rename to element later;
	private ParameterUsageProvider usageProvider;

	public ConverterConstructorParameter(MutableTypeMirror type, String name, MutableType usage, PropagationType propagationType, MutableProcessingEnvironment processingEnv) {
		super(type, name);
		this.propagationType = propagationType;
		
		if (usage != null) {
			this.usage = usage;
		} else {
			this.usage = processingEnv.getTypeUtils().getReference(null, name);
		}
		
		this.usageProvider = null;
	}

	public ConverterConstructorParameter(MutableTypeMirror type, String name, ParameterUsageProvider usageProvider, PropagationType propagationType) {
		super(type, name);
		this.propagationType = propagationType;
		this.usage = null;
		this.usageProvider = usageProvider;
	}

	public ConverterConstructorParameter(ParameterElement parameter, MutableProcessingEnvironment processingEnvironment) {
		this(parameter.type, parameter.name, parameter.usage, parameter.propagationType, processingEnvironment);
		
		if (parameter.usage == null) {
			this.usageProvider = parameter.usageProvider;
		}
	}

	public void merge(ParameterElement parameter) {
		if (parameter.usage != null) {
			this.usage = parameter.usage;
		} else {
			this.usageProvider = parameter.usageProvider;
		}
		
		setName(parameter.name);
	}
	
	public ParameterElement toParameterElement() {
		if (usage != null) {
			return new ParameterElement(getType(), getName(), usage, propagationType, null);
		}
		
		return new ParameterElement(getType(), getName(), usageProvider, propagationType);
	}
	
	public MutableType getUsage(ParameterUsageContext context) {
		if (usage != null) {
			return usage;
		}
		
		return usageProvider.getUsage(context);
	}
	
	public ConverterConstructorParameter getSameParameter() {
		return sameParameter;
	}

	public ConverterConstructorParameter setSameParameter(ConverterConstructorParameter sameParameter) {
		this.sameParameter = sameParameter;
		return this;
	}
	
	public void setPropagationType(PropagationType propagationType) {
		this.propagationType = propagationType;
	}

	public PropagationType getPropagationType() {
		return this.propagationType;
	}
}