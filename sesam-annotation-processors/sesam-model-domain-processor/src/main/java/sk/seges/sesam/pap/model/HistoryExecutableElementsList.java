package sk.seges.sesam.pap.model;

import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

import javax.lang.model.element.ExecutableElement;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class HistoryExecutableElementsList extends AbstractElementsList<ExecutableElement> {

	private ExecutableElementsList removedElements = new ExecutableElementsList(null);

	public AbstractElementsList<ExecutableElement> getRemovedElements() {
		return removedElements;
	}

	public HistoryExecutableElementsList(List<String> ignoredElements) {
		super(ignoredElements);
	}

	@Override
	protected String toString(ExecutableElement executableElement) {
		return TransferObjectHelper.getFieldPath(executableElement);
	}

	@Override
	public boolean add(ExecutableElement element) {
		if (super.add(element)) {
			removedElements.remove(element);
			return true;
		}
		return false;
	}

	@Override
	public void add(int index, ExecutableElement element) {
		removedElements.remove(element);
		super.add(index, element);
	}

	public boolean remove(Object o) {
		if (super.remove(o)) {
			removedElements.add((ExecutableElement) o);
			return true;
		}

		return false;
	}

	@Override
	public ExecutableElement remove(int index) {
		ExecutableElement removedElement = super.remove(index);

		if (removedElement != null) {
			removedElements.add(removedElement);
		}

		return removedElement;
	}
}
