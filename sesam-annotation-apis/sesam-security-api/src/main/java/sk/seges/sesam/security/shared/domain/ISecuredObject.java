package sk.seges.sesam.security.shared.domain;

import java.io.Serializable;

import sk.seges.sesam.domain.IDomainObject;

public interface ISecuredObject<T> extends Serializable, IDomainObject<T> {

	public static final String ACL_OBJECT_READ = "ACL_OBJECT_READ";
	public static final String ACL_OBJECT_WRITE = "ACL_OBJECT_WRITE";
	public static final String ACL_OBJECT_DELETE = "ACL_OBJECT_DELETE";
	public static final String ACL_OBJECT_ADMIN = "ACL_OBJECT_ADMIN";
	
	Long getIdForACL();

	Class<?> getSecuredClass();

	ISecuredObject<?> getParent();
}
