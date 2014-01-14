package sk.seges.sesam.pap.model;

import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public class ExecutableElementsList extends AbstractElementsList<ExecutableElement> {

	public ExecutableElementsList(List<String> ignoredElements) {
		super(ignoredElements);
	}

	@Override
	protected String toString(ExecutableElement executableElement) {
		return TransferObjectHelper.getFieldPath(executableElement);
	}

}
