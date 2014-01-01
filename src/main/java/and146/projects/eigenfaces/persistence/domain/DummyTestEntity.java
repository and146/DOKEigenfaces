/**
 * 
 */
package and146.projects.eigenfaces.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author neonards
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class DummyTestEntity extends DomainObject {
	
	@Column(nullable=false)
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
