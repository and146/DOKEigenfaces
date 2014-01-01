/**
 * 
 */
package and146.projects.eigenfaces;

import java.util.List;
import java.util.Map;

import and146.projects.eigenfaces.domain.TrainingSet;

/**
 * Interface for face recognizing.
 * 
 * @author neonards
 *
 */
public interface IFaceRecognizer {
	
	/**
	 * Test scenarios for the testPrediction() method.
	 *
	 */
	public enum RecognizerTestScenario{
		/**
		 * Attempts to predict/recognize each face already present
		 * in the training set. Supposed accuracy is 100%.
		 */
		EXISTING_TRAINING_FACES,
		
		/**
		 * Requires at least two photos of each person in the training
		 * set. During the training, one of the faces is ignored and used
		 * as a test face for recognizer. Supposed accuracy is close to 100%.
		 * 
		 * Note, that this scenario requires re-training, which requires a training
		 * set to be passed as argument before.
		 */
		SKIPPED_TRAINING_FACES
	}
	
	/**
	 * Training method for face recognizer.
	 * In this method. it is up to the user to validate the path!
	 * 
	 * @param trainingData Map<personId, List<photoFilePath>>
	 */
	public void train(Map<String, List<String>> trainingData);
	
	/**
	 * Training method for face recognizer
	 * 
	 * @param trainingXmlFilePath Path to the XML cotaining training data.
	 */
	public void train(String trainingXmlFilePath);
	
	/** 
	 * Gets a prediction from a FaceRecognizer.
	 * 
	 * @param fileName test file name
	 * @return ID of predicted person
	 */
    public String predict(String fileName);

    /** 
     * Predicts the label and confidence for a given test file.
     * 
     * @param fileName test file name
     * @param confidence
     * @return ID of predicted person
     */
    public String predict(String fileName, double confidence);
    
    /**
     * Loads existing training set
     * 
     * @param trainingSet
     */
    public void loadTrainingSet(TrainingSet trainingSet);
    
    /**
     * Tests the recognizer's confidence 
     * 
     * @param scenario Required test scenario
     */
    public void testPrediction(RecognizerTestScenario scenario);
    
    /**
     * Getter for the training set
     */
    public TrainingSet getTrainingSet();
}
