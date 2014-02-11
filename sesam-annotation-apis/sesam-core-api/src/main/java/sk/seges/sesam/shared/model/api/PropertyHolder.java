package sk.seges.sesam.shared.model.api;

import java.io.Serializable;
import java.util.Date;

public class PropertyHolder implements Serializable{

	private static final long serialVersionUID = 3632843192430935592L;
	
	private ValueType valueType;

	private Boolean booleanValue;
	private String stringValue;
	private SessionArrayHolder arrayValue;
	private TransferableEnum enumValue;
	private Date dateValue;
	private Long longValue;

	public PropertyHolder() {

	};

	public PropertyHolder(Object value) {
		setValue(value);
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public Long getLongValue() {
		if (valueType != null && !valueType.equals(ValueType.LONG)) {
			throw new RuntimeException("Mismatched type, expecting Long value, current type is " + valueType.name());
		}
		return longValue;
	}

	public Date getDateValue() {
		if (valueType != null && !valueType.equals(ValueType.DATE)) {
			throw new RuntimeException("Mismatched type, expecting Date value, current type is " + valueType.name());
		}
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public Boolean getBooleanValue() {
		if (valueType != null && !valueType.equals(ValueType.BOOLEAN)) {
			throw new RuntimeException("Mismatched type, expecting Boolean value, current type is " + valueType.name());
		}
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public String getStringValue() {
		if (valueType != null && !valueType.equals(ValueType.STRING)) {
			throw new RuntimeException("Mismatched type, expecting String value, current type is " + valueType.name());
		}
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public SessionArrayHolder getArrayValue() {
		if (valueType != null && !valueType.equals(ValueType.ARRAY)) {
			throw new RuntimeException("Mismatched type, expecting Array value, current type is " + valueType.name());
		}
		return arrayValue;
	}

	public void setArrayValue(SessionArrayHolder arrayValue) {
		this.arrayValue = arrayValue;
	}

	public TransferableEnum getEnumValue() {
		if (valueType != null && !valueType.equals(ValueType.ENUM)) {
			throw new RuntimeException("Mismatched type, expecting Enum value, current type is " + valueType.name());
		}
		return enumValue;
	}

	public void setEnumValue(TransferableEnum enumValue) {
		this.enumValue = enumValue;
	}

	public PropertyHolder(Object value, ValueType valueType) {
		this.valueType = valueType;
		valueType.setValue(this, value);
	}

	public PropertyHolder setValue(Object value) {
		if(ValueType.valueFor(value) != null){
			this.valueType = ValueType.valueFor(value).setValue(this, value);
		}		
		return this;
	}

	public Object getValue() {
		if (valueType == null) {
			return null;
		}
		return valueType.getValue(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PropertyHolder)) return false;

		PropertyHolder that = (PropertyHolder) o;

		if (arrayValue != null ? !arrayValue.equals(that.arrayValue) : that.arrayValue != null) return false;
		if (booleanValue != null ? !booleanValue.equals(that.booleanValue) : that.booleanValue != null) return false;
		if (dateValue != null ? !dateValue.equals(that.dateValue) : that.dateValue != null) return false;
		if (enumValue != null ? !enumValue.equals(that.enumValue) : that.enumValue != null) return false;
		if (longValue != null ? !longValue.equals(that.longValue) : that.longValue != null) return false;
		if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) return false;
		if (valueType != that.valueType) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = valueType != null ? valueType.hashCode() : 0;
		result = 31 * result + (booleanValue != null ? booleanValue.hashCode() : 0);
		result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
		result = 31 * result + (arrayValue != null ? arrayValue.hashCode() : 0);
		result = 31 * result + (enumValue != null ? enumValue.hashCode() : 0);
		result = 31 * result + (dateValue != null ? dateValue.hashCode() : 0);
		result = 31 * result + (longValue != null ? longValue.hashCode() : 0);
		return result;
	}
}