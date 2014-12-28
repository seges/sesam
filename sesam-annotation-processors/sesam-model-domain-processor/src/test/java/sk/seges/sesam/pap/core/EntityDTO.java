package sk.seges.sesam.pap.core;
import java.io.Serializable;
import java.util.Date;

import javax.annotation.Generated;

import sk.seges.sesam.pap.model.annotation.TransferObjectMapping;

@SuppressWarnings("serial")
@TransferObjectMapping(dtoClass = EntityDTO.class,
		domainClassName = "sk.seges.sesam.pap.model.Entity", 
		configurationClassName = "sk.seges.sesam.pap.core.EntityDTOConfiguration", 
		generateConverter = false, generateDto = false, 
		converterClassName = "sk.seges.sesam.pap.core.EntityDTOConverter")
@Generated(value = "sk.seges.sesam.pap.model.TransferObjectProcessor")
public class EntityDTO implements Serializable {
	 
	public static final String DATE = "date";
	
	private Date date;
	
	public static final String NICEURL = "niceurl";
	
	private String niceurl;
	
	public static final String VALUE = "value";
	
	private Long value;
	
	public static final String WEB_ID = "webId";
	
	private String webId;
	
	public EntityDTO() {}
	
	public EntityDTO(Date date, String niceurl, Long value, String webId) {
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
	 
}
