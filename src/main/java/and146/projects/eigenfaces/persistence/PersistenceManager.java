/**
 * 
 */
package and146.projects.eigenfaces.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neonards
 * Data persistence manager.
 */
public class PersistenceManager {
	
	public static final String ENTITY_MANAGER_FACTORY_ID = "eigenfacesemf";
	
	private static Logger log = LoggerFactory.getLogger(PersistenceManager.class);
	
	private static EntityManagerFactory entityManagerFactory;
	private static EntityManager entityManager;
	
	/**
	 * init method - call this during application initialization
	 */
	public static final void init() {
		log.debug("Initializing persistance");
		entityManagerFactory = Persistence.createEntityManagerFactory(ENTITY_MANAGER_FACTORY_ID);
		entityManager = entityManagerFactory.createEntityManager();
		log.debug("Entity manager created.");
	}
	
	/**
	 * Getter for JPA EntityManager
	 * @return
	 */
	public static EntityManager getEntityManager() {
		return entityManager;
	}
	
	/**
	 * Cleans up the whole database by performing a delete on each
	 * entity
	 */
	public static void cleanupDatabase() {
		log.debug("Cleaning up database ...");
		getEntityManager().getTransaction().begin();
		for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
			/*final String className = entity.getName();
		    log.debug("Trying delete * from: " + className);
		    Query q = entityManager.createQuery("delete from " + className + " c");
		    q.executeUpdate();*/
			final String className = entity.getName();
		    log.debug("Trying delete * from: " + className);
		    Query q = entityManager.createQuery("select c from " + className + " c");
		    List<?> r = q.getResultList();
		    
		    for (Object o : r) {
		    	getEntityManager().remove(o);
		    }

		}
		getEntityManager().getTransaction().commit();
	}
}
