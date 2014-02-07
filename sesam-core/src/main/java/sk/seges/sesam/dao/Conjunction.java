/**
 * 
 */
package sk.seges.sesam.dao;

/**
 * @author ladislav.gazo
 */
public class Conjunction extends Junction {
	private static final long serialVersionUID = 7806771561676701600L;

	public Conjunction() {	}
	
	public Junction and(Criterion criterion) {
		return add(criterion);
	}

	@Override
	public String getOperation() {
		return "conjunction";
	}
}
