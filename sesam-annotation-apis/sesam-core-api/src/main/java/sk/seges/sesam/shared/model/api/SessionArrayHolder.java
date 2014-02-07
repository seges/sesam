package sk.seges.sesam.shared.model.api;

import java.io.Serializable;

public class SessionArrayHolder implements Serializable {
	private static final long serialVersionUID = 1617109303404115704L;
	
	private String[] values;
	
	public SessionArrayHolder() {}
	
	public SessionArrayHolder(String[] values) {
		this.values = values;
	}
	
	public void setValues(String[] values) {
		this.values = values;
	}
	
	public String[] getValues() {
		return values;
	}
}
