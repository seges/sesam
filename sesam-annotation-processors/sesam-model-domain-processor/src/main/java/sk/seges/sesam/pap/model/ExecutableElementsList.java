package sk.seges.sesam.pap.model;

import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

import javax.lang.model.element.ExecutableElement;

public class ExecutableElementsList extends AbstractElementsList<ExecutableElement> {

	@Override
	protected String toString(ExecutableElement executableElement) {
		return TransferObjectHelper.getFieldPath(executableElement);
	}

}
