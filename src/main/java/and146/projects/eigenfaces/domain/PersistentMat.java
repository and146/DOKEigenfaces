/**
 * 
 */
package and146.projects.eigenfaces.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;

import and146.projects.eigenfaces.persistence.domain.DomainObject;

/**
 * @author neonards
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class PersistentMat extends DomainObject {
	
	private static final long serialVersionUID = 1L;
	
	@Lob
	@Column(length=100000)
	double[][][] matrix;
	
	@Column(nullable=false)
	int type;
	
	public double[][][] getMatrix() {
		return matrix;
	}
	
	public void setMatrix(double[][][] matrix) {
		this.matrix = matrix;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
}
