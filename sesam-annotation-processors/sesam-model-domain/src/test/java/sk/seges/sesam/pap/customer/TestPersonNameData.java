/**
 * 
 */
package sk.seges.sesam.pap.customer;

import java.io.Serializable;


/**
 * @author ladislav.gazo
 */
public interface TestPersonNameData extends Serializable {
	public static final String FIRST_NAME = "firstName";
	public static final String SURNAME = "surname";
	public static final String SALUTATION = "salutation";

	String getFirstName();
	void setFirstName(String firstName);
	
	String getSurname();
	void setSurname(String surname);
	
	TestSalutation getSalutation();
	void setSalutation(TestSalutation salutation);
}
