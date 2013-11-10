package sk.seges.sesam.core.pap.utils;

import java.util.ArrayList;
import java.util.List;

import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.model.api.PropagationType;

public enum ParametersFilter {
	PROPAGATED_MUTABLE {
		@Override
		public ParameterElement[] filterParameters(ParameterElement[] elements) {
			return filterByPropagationParameters(elements, PropagationType.PROPAGATED_MUTABLE);
		}
	},
	PROPAGATED_IMUTABLE {
		@Override
		public ParameterElement[] filterParameters(ParameterElement[] elements) {
			return filterByPropagationParameters(elements, PropagationType.PROPAGATED_IMUTABLE);
		}
	},
	INSTANTIATED {
		@Override
		public ParameterElement[] filterParameters(ParameterElement[] elements) {
			return filterByPropagationParameters(elements, PropagationType.INSTANTIATED);
		}
	};

	public abstract ParameterElement[] filterParameters(ParameterElement[] elements);

	private static ParameterElement[] filterByPropagationParameters(ParameterElement[] elements, PropagationType propagationType) {
		List<ParameterElement> result = new ArrayList<ParameterElement>();
		
		for (ParameterElement element: elements) {
			if (element.getPropagationType().equals(propagationType)) {
				result.add(element);
			}
		}
		return result.toArray(new ParameterElement[] {});
	}
}
