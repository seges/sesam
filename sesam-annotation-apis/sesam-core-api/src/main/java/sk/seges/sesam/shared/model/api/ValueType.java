package sk.seges.sesam.shared.model.api;


import java.util.Date;

public enum ValueType {

	BOOLEAN {
		@Override
		public Object getValue(PropertyHolder propertyHolder) {
			return propertyHolder.getBooleanValue();
		}

		@Override
		public ValueType setValue(PropertyHolder propertyHolder, Object value) {
			propertyHolder.setBooleanValue((Boolean) value);
			return ValueType.BOOLEAN;
		}

		@Override
		public Class<?> appliesFor() {
			return Boolean.class;
		}
	},
	DATE {
		@Override
		public Object getValue(PropertyHolder propertyHolder) {
			return propertyHolder.getDateValue();
		}

		@Override
		public ValueType setValue(PropertyHolder propertyHolder, Object value) {
			propertyHolder.setDateValue((Date) value);
			return ValueType.DATE;
		}

		@Override
		public Class<?> appliesFor() {
			return Date.class;
		}
	},
	LONG {
		@Override
		public Object getValue(PropertyHolder propertyHolder) {
			return propertyHolder.getLongValue();
		}

		@Override
		public ValueType setValue(PropertyHolder propertyHolder, Object value) {
			propertyHolder.setLongValue((Long) value);
			return ValueType.LONG;
		}

		@Override
		public Class<?> appliesFor() {
			return Long.class;
		}
	},
	STRING {
		@Override
		public Object getValue(PropertyHolder propertyHolder) {
			return propertyHolder.getStringValue();
		}

		@Override
		public ValueType setValue(PropertyHolder propertyHolder, Object value) {
			propertyHolder.setStringValue((String) value);
			return ValueType.STRING;
		}

		@Override
		public Class<?> appliesFor() {
			return String.class;
		}
	},
	ARRAY {
		@Override
		public Object getValue(PropertyHolder propertyHolder) {
			return propertyHolder.getArrayValue();
		}

		@Override
		public ValueType setValue(PropertyHolder propertyHolder, Object value) {
			propertyHolder.setArrayValue((SessionArrayHolder) value);
			return ValueType.ARRAY;
		}

		@Override
		public Class<?> appliesFor() {
			return SessionArrayHolder.class;
		}
	},
	ENUM {
		@Override
		public Object getValue(PropertyHolder propertyHolder) {
			return propertyHolder.getEnumValue();
		}

		@Override
		public ValueType setValue(PropertyHolder propertyHolder, Object value) {
			propertyHolder.setEnumValue((TransferableEnum) value);
			return ValueType.ENUM;
		}

		@Override
		public Class<?> appliesFor() {
			return Enum.class;
		}

		@Override
		public Class<?>[] interfaceTypes() {
			return new Class<?>[] {
				TransferableEnum.class
			};
		}
	};

	public abstract Object getValue(PropertyHolder propertyHolder);
	public abstract ValueType setValue(PropertyHolder propertyHolder, Object value);
	public abstract Class<?> appliesFor();

	public Class<?>[] interfaceTypes() {
		return new Class<?>[0];
	}

	private static boolean implementTypes(Class<?> objClass, Class<?>[] interfaces) {

		for (Class<?> interfaceClass: interfaces) {
			if (!interfaceClass.equals(objClass)) {
				return false;
			}
		}

		return true;
	}

	public static ValueType valueFor(Object obj) {
		if (obj == null) {
			return ValueType.STRING;
		}

		for (ValueType valueType: ValueType.values()) {
			if (valueType.appliesFor().equals(obj.getClass()) && implementTypes(obj.getClass(), valueType.interfaceTypes())) {
				return valueType;
			}
		}

		if (obj.getClass().isEnum() && implementTypes(obj.getClass(), ValueType.ENUM.interfaceTypes())) {
			return ValueType.ENUM;
		}

		throw new RuntimeException("Not supported class " + obj.getClass().getName());
	}
}