package sk.seges.sesam.pap.model;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractElementsList<T extends Element> extends ArrayList<T> {

	protected final List<String> elements = new ArrayList<String>();

	protected abstract String toString(T t);

	@Override
	public T get(int index) {
		return super.get(index);
	}

	@Override
	public boolean add(T element) {
		String elementString = toString(element);

		if (elements.contains(elementString)) {
			return false;
		}
		elements.add(elementString);
		return super.add(element);
	}

	@Override
	public void add(int index, T element) {
		String elementString = toString(element);

		if (elements.contains(elementString)) {
			return;
		}
		elements.add(index, elementString);
		super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T element: c) {
			add(element);
		}

		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		int i = 0;
		for (T element: c) {
			int size = size();
			add(index + i, element);
			i += size() - size;
		}

		return true;
	}

	@Override
	public boolean remove(Object o) {
		String elementString = toString((T)o);
		int index = elements.indexOf(elementString);

		if (index == -1) {
			return false;
		}
		elements.remove(elementString);
		return super.remove(index) != null;
	}

	@Override
	public T remove(int index) {
		elements.remove(index);
		return super.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object o: c) {
			remove(o);
		}
		return true;
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public boolean contains(Object o) {
		return elements.contains(toString((T)o));
	}
}