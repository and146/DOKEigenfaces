/**
 * 
 */
package and146.projects.eigenfaces.domain;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.opencv.core.Mat;

import and146.projects.eigenfaces.persistence.domain.DomainObject;

/**
 * Training set for recognizer
 * 
 * @author neonards
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class TrainingSet extends DomainObject{
	
	private static final long serialVersionUID = 1L;

	/**
	 * All faces in the set (photos, not persons)
	 */
	@Column(nullable=false)
	private int facesCount = 0;
	
	/**
	 * All persons in the set
	 */
	@Column(nullable=false)
	private int personsCount = 0;
	
	/**
	 * All training faces
	 */
	@OneToMany
	private List<PersistentMat> trainingFacesMatrices;
	
	/**
	 * All persons IDs
	 */
	@Column(length=100000)
	private String[] personIds;
	
	/**
	 * Eigenvectors
	 */
	@OneToOne
	private PersistentMat persistedEigenVectors;
	
	/**
	 * Average face
	 */
	@OneToOne
	private PersistentMat peristedAverageFace;
	
	/**
	 * Projected training faces (PCA subspace)
	 */
	@OneToOne
	private PersistentMat peristedProjectedTrainingFaces;
	
	//
	
	public int getFacesCount() {
		return facesCount;
	}
	
	public void setFacesCount(int facesCount) {
		this.facesCount = facesCount;
	}
	
	public int getPersonsCount() {
		return personsCount;
	}
	
	public void setPersonsCount(int personsCount) {
		this.personsCount = personsCount;
	}
	
	public String[] getPersonIds() {
		return personIds;
	}
	
	public void setPersonIds(String[] personIds) {
		this.personIds = personIds;
	}
	
	public PersistentMat getPersistedEigenVectors() {
		return persistedEigenVectors;
	}
	
	public void setPersistedEigenVectors(PersistentMat persistedEigenVectors) {
		this.persistedEigenVectors = persistedEigenVectors;
	}
	
	public PersistentMat getPersistedAverageFace() {
		return peristedAverageFace;
	}
	
	public void setPersistedAverageFace(PersistentMat averageFace) {
		this.peristedAverageFace = averageFace;
	}
	
	public PersistentMat getPersistedProjectedTrainingFaces() {
		return peristedProjectedTrainingFaces;
	}
	
	public void setPersistedProjectedTrainingFaces(PersistentMat projectedTrainingFaces) {
		this.peristedProjectedTrainingFaces = projectedTrainingFaces;
	}
	
	//
	
	/**
	 * 
	 * @return Persisted training faces converted to OpenCV's Mat array
	 */
	public Mat[] getTrainingFaces() {
		Mat[] trainingFaces = new Mat[trainingFacesMatrices.size()];
		
		for (int i = 0; i < getTrainingFacesMatrices().size(); i++) {
			
			PersistentMat pm = getTrainingFacesMatrices().get(i);
			if (pm.getMatrix().length > 0) {
				
				int pmRows = pm.getMatrix().length;
				int pmCols = pm.getMatrix()[0].length;
				
				Mat mat = new Mat();
				mat.create(pmRows, pmCols, pm.getType());
				for (int j = 0; j < pmRows; j++) {
					for (int k = 0; k < pmCols; k++) {
						mat.put(j, k, pm.getMatrix()[j][k]);
					}
				}
				
				trainingFaces[i] = mat;
			}
		}
		
		return trainingFaces;
	}
	
	/**
	 * Setter for training faces (also converts the
	 * training faces to its persisted variant)
	 * @param trainingFaces
	 */
	public void setTrainingFaces(Mat[] trainingFaces) {
		getTrainingFacesMatrices().clear();
		
		for (int i = 0; i < trainingFaces.length; i++) {
			Mat mat = trainingFaces[i];
			/*PersistentMat pm = new PersistentMat();
			pm.setType(mat.type());
			
			double[][][] matrix = new double[mat.rows()][mat.cols()][mat.get(0, 0).length];
			
			for (int j = 0; j < mat.rows(); j++) {
				for (int k = 0; k < mat.cols(); k ++) {
					matrix[j][k] = mat.get(j, k);
				}
			}
			pm.setMatrix(matrix);*/
			getTrainingFacesMatrices().add(convertMatToPersistentMat(mat));
		}
	}
	
	/**
	 * 
	 * @return persisted training faces
	 */
	public List<PersistentMat> getTrainingFacesMatrices() {
		if (trainingFacesMatrices == null)
			trainingFacesMatrices = new LinkedList<PersistentMat>();
		
		return trainingFacesMatrices;
	}
	
	/**
	 * setter for persisted training faces matrices
	 * @param trainingFacesMatrices
	 */
	public void setTrainingFacesMatrices(
			List<PersistentMat> trainingFacesMatrices) {
		this.trainingFacesMatrices = trainingFacesMatrices;
	}
	
	/**
	 * 
	 * @return persisted eigen vectors converted to OpenCV's Mat
	 */
	public Mat getEigenVectors() {
		return convertPersistentMatToMat(getPersistedEigenVectors());
	}
	
	/**
	 * setter for persisted eigen vectors
	 * @param eigenVectors
	 */
	public void setEigenVectors(Mat eigenVectors) {
		setPersistedEigenVectors(convertMatToPersistentMat(eigenVectors));
	}
	
	/**
	 * 
	 * @return persisted average face converted to OpenCV's Mat
	 */
	public Mat getAverageFace() {
		return convertPersistentMatToMat(getPersistedAverageFace());
	}
	
	/**
	 * setter for persisted average face
	 * @param averageFace
	 */
	public void setAverageFace(Mat averageFace) {
		setPersistedAverageFace(convertMatToPersistentMat(averageFace));
	}
	
	/**
	 * 
	 * @return persisted projected training faces converted to OpenCV's Mat
	 */
	public Mat getProjectedTrainingFaces() {
		return convertPersistentMatToMat(getPersistedProjectedTrainingFaces());
	}
	
	/**
	 * setter for projected training faces
	 * @param projectedTrainingFaces
	 */
	public void setProjectedTrainingFaces(Mat projectedTrainingFaces) {
		setPersistedProjectedTrainingFaces(convertMatToPersistentMat(projectedTrainingFaces));
	}
	
	//
	
	/**
	 * Converts OpenCV's Mat to a PersistentMat
	 * @param mat
	 * @return
	 */
	private PersistentMat convertMatToPersistentMat(Mat mat) {
		PersistentMat pm = new PersistentMat();
		pm.setType(mat.type());
		
		double[][][] matrix = new double[mat.rows()][mat.cols()][mat.get(0, 0).length];
		
		for (int j = 0; j < mat.rows(); j++) {
			for (int k = 0; k < mat.cols(); k ++) {
				matrix[j][k] = mat.get(j, k);
			}
		}
		pm.setMatrix(matrix);
		
		return pm;
	}
	
	private Mat convertPersistentMatToMat(PersistentMat pm) {
		Mat mat = null;
		
		if (pm != null) {
			int rows = pm.getMatrix().length;
			int cols = pm.getMatrix()[0].length;
			
			mat = new Mat();
			mat.create(rows, cols, pm.getType());
			
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					mat.put(i, j, pm.getMatrix()[i][j]);
				}
			}
		}
		
		return mat;
	}
}
