package sk.seges.sesam.pap.equals;
import java.io.Serializable;
import java.util.Date;

import javax.annotation.Generated;

import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;

@SuppressWarnings("serial")
@TransferObjectMapping(dtoClass = EntityWithEqualsDTO.class,
		domainClassName = "sk.seges.sesam.pap.model.Entity", 
		configurationClassName = "sk.seges.sesam.pap.equals.EntityWithEqualsDTOConfiguration", 
		generateConverter = false, generateDto = false, 
		converterClassName = "sk.seges.sesam.pap.equals.EntityWithEqualsDTOConverter")
@Generated(value = "sk.seges.sesam.pap.model.TransferObjectProcessor")
public class EntityWithEqualsDTO implements Serializable {
	 
	public static final String DATE = "date";
	
	private Date date;
	
	public static final String NICEURL = "niceurl";
	
	private String niceurl;
	
	public static final String VALUE = "value";
	
	private Long value;
	
	public static final String WEB_ID = "webId";
	
	private String webId;
	
	public EntityWithEqualsDTO() {}
	
	public EntityWithEqualsDTO(Date date, String niceurl, Long value, String webId) {
		this.date = date;
		this.niceurl = niceurl;
		this.value = value;
		this.webId = webId;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	 
	public String getNiceurl() {
		return niceurl;
	}
	
	public void setNiceurl(String niceurl) {
		this.niceurl = niceurl;
	}
	 
	public Long getValue() {
		return value;
	}
	
	public void setValue(Long value) {
		this.value = value;
	}
	 
	public String getWebId() {
		return webId;
	}
	
	public void setWebId(String webId) {
		this.webId = webId;
	}
	 
	private boolean processingEquals = false;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityWithEqualsDTO other = (EntityWithEqualsDTO) obj;
		if (!_equalsSupport(date, other.date)) return false;
		if (!_equalsSupport(niceurl, other.niceurl)) return false;
		if (!_equalsSupport(value, other.value)) return false;
		if (!_equalsSupport(webId, other.webId)) return false;
		return true;
	}
	private boolean _equalsSupport(Object o1, Object o2) {
		if (o1 == null) {
			if (o2 != null) return false;
		} else if (!processingEquals) {
			processingEquals = true;
			if (!o1.equals(o2)) return processingEquals = false;
			else processingEquals = false;
		}
		return true;
	}
	 
}
