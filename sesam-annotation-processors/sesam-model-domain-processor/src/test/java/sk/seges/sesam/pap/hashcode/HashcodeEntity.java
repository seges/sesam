package sk.seges.sesam.pap.hashcode;

import sk.seges.sesam.pap.model.Entity;
import sk.seges.sesam.pap.validation.annotation.NotNull;

import java.util.Date;

/**
 * Created by PeterSimun on 27.12.2014.
 */
public class HashcodeEntity {

    private String webId;
    private String niceurl;

    private Date date;

    private Long value;

    public String getWebId() {
        return webId;
    }

    public void setWebId(String webId) {
        this.webId = webId;
    }

    public String getNiceurl() {
        return niceurl;
    }

    public void setNiceurl(String niceurl) {
        this.niceurl = niceurl;
    }

    @NotNull
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

}
