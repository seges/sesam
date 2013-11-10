package sk.seges.sesam.core.pap.writer;

import sk.seges.sesam.core.pap.model.mutable.api.*;

import java.util.HashSet;
import java.util.Set;

public class TypeExtractor {

	private final boolean typed;

	public TypeExtractor(boolean typed) {
		this.typed = typed;
	}

	public Set<MutableDeclaredType> extractDeclaredType(MutableTypeMirror type) {
		return extractDeclaredType(type, new HashSet<MutableDeclaredType>());
	}

	private Set<MutableDeclaredType> extractDeclaredType(MutableTypeMirror type, Set<MutableDeclaredType> types) {

		if (type instanceof MutableDeclaredType) {
			types.add((MutableDeclaredType) type);
			if (typed) {
				for (MutableTypeVariable variable : ((MutableDeclaredType) type).getTypeVariables()) {
					types.addAll(extractDeclaredType(variable));
				}
			}
			return types;
		}

		if (type instanceof MutableArrayType) {
			extractDeclaredType(((MutableArrayType) type).getComponentType(), types);
			return types;
		}

		if (type instanceof MutableTypeVariable) {
			MutableTypeVariable variable = ((MutableTypeVariable) type);
			Set<? extends MutableTypeMirror> lowerBounds = variable.getLowerBounds();

			for (MutableTypeMirror lowerBound : lowerBounds) {
				extractDeclaredType(lowerBound, types);
			}

			Set<? extends MutableTypeMirror> upperBounds = variable.getUpperBounds();

			for (MutableTypeMirror upperBound : upperBounds) {
				extractDeclaredType(upperBound, types);
			}

			return types;
		}

		if (type instanceof MutableWildcardType) {
			MutableWildcardType wildcard = (MutableWildcardType) type;

			if (wildcard.getExtendsBound() != null) {
				extractDeclaredType(wildcard.getExtendsBound(), types);
			}

			if (wildcard.getSuperBound() != null) {
				extractDeclaredType(wildcard.getSuperBound(), types);
			}

			return types;
		}

		return types;
	}

}
