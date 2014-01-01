/**
 * 
 */
package and146.projects.eigenfaces.persistence.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import and146.projects.eigenfaces.persistence.PersistenceManager;
import and146.projects.eigenfaces.persistence.domain.IPersistable;

/**
 * @author neonards
 *
 */
public abstract class Dao<T extends IPersistable> implements IGenericCRUDDao<T> {
	
	/**
	 * Creates a DAO for given class.
	 * @param clazz
	 * @return
	 */
	public static <U extends IPersistable> IGenericCRUDDao<U> getForClass(final Class<U> clazz) {
		return new Dao<U>(){

			@Override
			public U getById(long domainObjectId) {
				return getById(clazz, domainObjectId);
			}

			@Override
			public List<U> getAll() {
				return getAll(clazz);
			}
		};
	}

	@Override
	public void add(T domainObject) {
		getEntityManager().getTransaction().begin();
		getEntityManager().persist(domainObject);
		getEntityManager().getTransaction().commit();
	}

	@Override
	public void delete(T domainObject) {
		IPersistable domainObjectToDelete = (IPersistable) getById(domainObject.getId());
		getEntityManager().getTransaction().begin();
		getEntityManager().remove(domainObjectToDelete);
		getEntityManager().getTransaction().commit();
	}

	@Override
	public abstract T getById(long domainObjectId);;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected T getById(Class clazz, long domainObjectId) {
		return (T) getEntityManager().find(clazz, domainObjectId);
	}

	@Override
	public abstract List<T> getAll();
	
	protected List<T> getAll(Class clazz) {
		List<T> ret = null;
		for (EntityType<?> entity : getEntityManager().getMetamodel().getEntities()) {
		    final String className = entity.getName();
		    if (className.equals(clazz.getSimpleName())) {
		    	Query q = getEntityManager().createQuery("from " + className + " c");
		    	ret = (List<T>)q.getResultList();
		    }
		}
		
		if (ret == null)
			throw new IllegalStateException("No entity type for " + clazz + "found.");
		
		return ret;
	}
	
	@Override
	public void update(T domainObject) {
		getEntityManager().getTransaction().begin();
		getEntityManager().merge(domainObject);
		getEntityManager().getTransaction().commit();
	}
	
	/**
	 * 
	 * @return EntityManager instance
	 */
	protected EntityManager getEntityManager() {
		return PersistenceManager.getEntityManager();
	}
	
}
