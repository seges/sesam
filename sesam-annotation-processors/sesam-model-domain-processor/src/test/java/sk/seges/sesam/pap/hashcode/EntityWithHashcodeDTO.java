package sk.seges.sesam.pap.hashcode;
import java.io.Serializable;
import java.util.Date;

import javax.annotation.Generated;

import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;

@SuppressWarnings("serial")
@TransferObjectMapping(dtoClass = EntityWithHashcodeDTO.class,
		domainClassName = "sk.seges.sesam.pap.model.Entity", 
		configurationClassName = "sk.seges.sesam.pap.hashcode.EntityWithHashcodeDTOConfiguration", 
		generateConverter = false, generateDto = false, 
		converterClassName = "sk.seges.sesam.pap.hashcode.EntityWithHashcodeDTOConverter")
@Generated(value = "sk.seges.sesam.pap.model.TransferObjectProcessor")
public class EntityWithHashcodeDTO implements Serializable {
	 
	public static final String DATE = "date";
	
	private Date date;
	
	public static final String NICEURL = "niceurl";
	
	private String niceurl;
	
	public static final String VALUE = "value";
	
	private Long value;
	
	public static final String WEB_ID = "webId";
	
	private String webId;
	
	public EntityWithHashcodeDTO() {}
	
	public EntityWithHashcodeDTO(Date date, String niceurl, Long value, String webId) {
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
	 
	private boolean processingHashCode = false;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = _hashCodeSupport(date, prime, result);
		result = _hashCodeSupport(niceurl, prime, result);
		result = _hashCodeSupport(value, prime, result);
		result = _hashCodeSupport(webId, prime, result);
		return result;
	}
	private int _hashCodeSupport(Object o1, int prime, int result) {
		if (!processingHashCode) {
			processingHashCode = true;
			result = 
			prime * result + ((o1 == null) ? 0 : o1.hashCode());processingHashCode = false;
		}
		return result;
	}
	 
}
