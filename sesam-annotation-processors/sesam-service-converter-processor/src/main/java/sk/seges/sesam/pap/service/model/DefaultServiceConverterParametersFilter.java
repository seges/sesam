package sk.seges.sesam.pap.service.model;

import java.util.ArrayList;
import java.util.List;

import sk.seges.sesam.core.pap.model.ConverterConstructorParameter;
import sk.seges.sesam.core.pap.model.api.PropagationType;

public class DefaultServiceConverterParametersFilter implements ServiceConverterParametersFilter {

	public DefaultServiceConverterParametersFilter() {
	}

	public List<ConverterConstructorParameter> getPropagatedParameters(List<ConverterConstructorParameter> parameters) {
		List<ConverterConstructorParameter> result = new ArrayList<ConverterConstructorParameter>();
		for (ConverterConstructorParameter parameter : parameters) {
			if (parameter.getPropagationType().equals(PropagationType.PROPAGATED_IMUTABLE)) {
				result.add(parameter);
			}
		}

		return result;
	}
}