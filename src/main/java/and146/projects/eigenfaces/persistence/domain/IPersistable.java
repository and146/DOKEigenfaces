/**
 * 
 */
package and146.projects.eigenfaces.persistence.domain;

import java.io.Serializable;

/**
 * Interface that says that its implementation will be persistable
 * 
 * @author neonards
 *
 */
public interface IPersistable extends Serializable {
	public long getId();
	public void setId(long id);
}
