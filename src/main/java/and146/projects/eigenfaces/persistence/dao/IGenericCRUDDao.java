/**
 * 
 */
package and146.projects.eigenfaces.persistence.dao;

import java.util.List;

import and146.projects.eigenfaces.persistence.domain.DomainObject;
import and146.projects.eigenfaces.persistence.domain.IPersistable;

/**
 * @author neonards
 *
 */
public interface IGenericCRUDDao<T extends IPersistable> {
	public void add(T domainObject);
	public void delete(T domainObject);
	public T getById(long domainObjectId);
	public List<T> getAll();
	public void update(T domainObject);
	
}
