/**
 * 
 */
package sk.seges.sesam.pap.customer;



/**
 * @author ladislav.gazo
 */
public class TestPersonNameCore implements TestPersonNameData {
	private static final long serialVersionUID = -2803845674455616494L;
	
	protected String firstName;
	protected String surname;
	protected TestSalutation salutation;
	
	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public TestSalutation getSalutation() {
		return salutation;
	}

	@Override
	public String getSurname() {
		return surname;
	}

	@Override
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public void setSalutation(TestSalutation salutation) {
		this.salutation = salutation;
	}

	@Override
	public void setSurname(String surname) {
		this.surname = surname;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((salutation == null) ? 0 : salutation.hashCode());
		result = prime * result + ((surname == null) ? 0 : surname.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestPersonNameCore other = (TestPersonNameCore) obj;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (salutation == null) {
			if (other.salutation != null)
				return false;
		} else if (!salutation.equals(other.salutation))
			return false;
		if (surname == null) {
			if (other.surname != null)
				return false;
		} else if (!surname.equals(other.surname))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "PersonNameDto [firstName=" + firstName + ", salutation=" + salutation + ", surname="
				+ surname + "]";
	}
	
}
