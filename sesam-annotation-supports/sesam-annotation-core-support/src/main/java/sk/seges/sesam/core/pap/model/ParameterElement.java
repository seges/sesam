package sk.seges.sesam.core.pap.model;

import javax.lang.model.element.ExecutableElement;

import sk.seges.sesam.core.pap.model.api.PropagationType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableType;
import sk.seges.sesam.core.pap.model.mutable.api.MutableTypeMirror;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;

public class ParameterElement {
	
	public interface ParameterUsageProvider {
		MutableType getUsage(ParameterUsageContext context);
	}

	public interface ParameterUsageContext {
		ExecutableElement getMethod();
	}

	final MutableType usage;
	final MutableTypeMirror type;
	final String name;
	final PropagationType propagationType;
	final ParameterUsageProvider usageProvider;
	
	public ParameterElement(MutableTypeMirror type, String name, MutableType usage, PropagationType propagationType, MutableProcessingEnvironment processingEnv) {
		this.type = type;
		this.name = name;
		this.propagationType = propagationType;

		if (usage != null) {
			this.usage = usage;
		} else {
			this.usage = processingEnv.getTypeUtils().getReference(null, name);
		}
		
		this.usageProvider = null;
	}

	public ParameterElement(MutableTypeMirror type, String name, ParameterUsageProvider usageProvider, PropagationType propagationType) {
		this.type = type;
		this.name = name;
		this.propagationType = propagationType;
		this.usage = null;
		this.usageProvider = usageProvider;
	}

	public MutableTypeMirror getType() {
		return type;
	}

	public MutableType getUsage(ParameterUsageContext context) {
		if (usage != null) {
			return usage;
		}
		
		return usageProvider.getUsage(context);
	}
	
	public String getName() {
		return name;
	}

	public boolean isConverter() {
		return false;
	}
	
	public PropagationType getPropagationType() {
		return propagationType;
	}
}