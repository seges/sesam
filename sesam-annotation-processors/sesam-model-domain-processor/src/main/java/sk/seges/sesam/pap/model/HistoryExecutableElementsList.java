package sk.seges.sesam.pap.model;

import sk.seges.sesam.pap.model.utils.TransferObjectHelper;

import javax.lang.model.element.ExecutableElement;
import java.util.Collection;

public class HistoryExecutableElementsList extends AbstractElementsList<ExecutableElement> {

	private ExecutableElementsList removedElements = new ExecutableElementsList();

	public AbstractElementsList<ExecutableElement> getRemovedElements() {
		return removedElements;
	}

	@Override
	protected String toString(ExecutableElement executableElement) {
		return TransferObjectHelper.getFieldPath(executableElement);
	}

	@Override
	public boolean add(ExecutableElement element) {
		removedElements.remove(element);
		return super.add(element);
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
