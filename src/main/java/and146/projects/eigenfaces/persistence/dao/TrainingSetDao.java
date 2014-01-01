/**
 * 
 */
package and146.projects.eigenfaces.persistence.dao;

import java.util.List;

import and146.projects.eigenfaces.domain.PersistentMat;
import and146.projects.eigenfaces.domain.TrainingSet;

/**
 * DAO for TrainingSet entity
 * 
 * @author neonards
 *
 */
public class TrainingSetDao extends Dao<TrainingSet> {

	@Override
	public void add(TrainingSet domainObject) {
		/*getEntityManager().getTransaction().begin();
		System.out.println("persisting...");
		System.out.println(getEntityManager().getFlushMode());
		int c= 1;
		for (PersistentMat pm : domainObject.getTrainingFacesMatrices()) {
			if (!(pm.getId() > 0)) {
				System.out.print("persisting " + c++ + "...");
				getEntityManager().persist(pm);
				System.out.println("done.");
			}
		}
		System.out.print("perstining main object...");
		getEntityManager().persist(domainObject);
		System.out.print("done, comitting...");
		getEntityManager().getTransaction().commit();
		System.out.println("done");*/
		IGenericCRUDDao<PersistentMat> pmDao = Dao.getForClass(PersistentMat.class);
		for (PersistentMat pm : domainObject.getTrainingFacesMatrices()) {
			if (!(pm.getId() > 0)) {
				pmDao.add(pm);
			}
		}
		
		if (!(domainObject.getPersistedEigenVectors().getId() > 0)) {
			pmDao.add(domainObject.getPersistedEigenVectors());
		}
		
		if (!(domainObject.getPersistedAverageFace().getId() > 0)) {
			pmDao.add(domainObject.getPersistedAverageFace());
		}
		
		if (!(domainObject.getPersistedProjectedTrainingFaces().getId() > 0)) {
			pmDao.add(domainObject.getPersistedProjectedTrainingFaces());
		}
		
		super.add(domainObject);
	}
	
	@Override
	public TrainingSet getById(long domainObjectId) {
		return getById(TrainingSet.class, domainObjectId);
	}

	@Override
	public List<TrainingSet> getAll() {
		return getAll(TrainingSet.class);
	}
	
}
