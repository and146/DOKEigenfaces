/**
 * 
 */
package and146.projects.eigenfaces;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import and146.projects.eigenfaces.domain.TrainingSet;
import and146.projects.eigenfaces.opencv.InMemoryGrayscaleImage;
import and146.projects.eigenfaces.opencv.InMemoryImage;

/**
 * FAce recognizer using PCA/eigenfaces algorithm.
 * 
 * @author neonards
 *
 */
public class EigenfacesRecognizer implements IFaceRecognizer {

	/**
	 * Default confidence level for a prediction phase. Only when the face
	 * is predicted (recognized) with a confidence greater than this value,
	 * it's marked as a possible match.
	 * 
	 * The confidence can be at <0...1> (<notmatched...matched>)
	 */
	public static final double DEFAULT_PREDICTION_CONFIDENCE = 0.5;
	
	private Logger log = LoggerFactory.getLogger(EigenfacesRecognizer.class);
	
	private Map<String, List<String>> inputTrainingData; 
	
	// TrainingSet container instance
	private TrainingSet trainingSet;
	
	// all faces in the set (photos, not persons)
	private int facesCount = 0;
	
	// all persons in the set
	private int personsCount = 0;
	
	// all eigen values count in the training set
	private int eigenValuesCount = 0;
	
	// training faces matrix
	private Mat[] trainingFaces;
	
	// person IDs per training image (trainingFaces:personIds = 1:1)
	private String[] personIds;
	
	// eigenvectors matrix
	private Mat eigenVectors;
	
	// average face matrix
	private Mat averageFace;
	
	// PCA subspace (holder for projected training faces)
	private Mat projectedTrainingFaces;
	
	@Override
	public void train(Map<String, List<String>> trainingData) {
		log.debug("Proceeding with training set...");
		
		/* initalization, variables */
		
		trainingSet = new TrainingSet();
		inputTrainingData = trainingData;
		
		/* preparation */
		facesCount = 0;
		personsCount = 0;
		eigenValuesCount = 0;
		
		// determine number of training faces and persons in the set
		for (String personId : trainingData.keySet()) {
			personsCount++;
			facesCount += trainingData.get(personId).size();
		}
		trainingSet.setFacesCount(facesCount);
		trainingSet.setPersonsCount(personsCount);
		trainingFaces = new Mat[trainingSet.getFacesCount()];
		log.info(String.format("Found %d faces of %d persons in the given training set.",
				trainingSet.getFacesCount(), trainingSet.getPersonsCount()));
		// add faces in the training set to a single Mat array
		int facesCounter = 0;
		personIds = new String[facesCount];
		for (String personId : trainingData.keySet()) {
			log.info(String.format("Processing person '%s'", personId));
			List<String> photos = trainingData.get(personId);
			
			for (String photoPathTmp : photos) {
				personIds[facesCounter] = personId;
				trainingFaces[facesCounter] = (new InMemoryImage(photoPathTmp)).getMat();
				facesCounter++;
			}
		}
		trainingSet.setTrainingFaces(trainingFaces);
		trainingSet.setPersonIds(personIds);
		// check we have enough of faces in the set
		if (trainingSet.getFacesCount() < 2)
			throw new IllegalStateException("At least 2 faces are required.");
		
		// perform the PCA
		doPca();
		
		//
	}

	@Override
	public void train(String trainingXmlFilePath) {
		log.debug("Proceeding with training set...");
		// parse the XML
		Map<String, List<String>> parsedTrainingSet = parseTrainingSetXml(trainingXmlFilePath);
		Map<String, List<String>> checkedTrainingSet = new LinkedHashMap<String, List<String>>();
		
		// check filepaths and convert them to absolute paths
		File xmlFile = new File(trainingXmlFilePath);
		for (String personId : parsedTrainingSet.keySet()) {
			log.info(String.format("Checking person '%s'", personId));
			
			checkedTrainingSet.put(personId, new LinkedList<String>());
			
			List<String> photos = parsedTrainingSet.get(personId);
			
			for (int i = 0; i < photos.size(); i++) {
				String photoPathTmp = photos.get(i);
				
				File photoFile = new File(xmlFile.getParentFile(), photoPathTmp);
				
				if (!photoFile.exists() || !photoFile.canRead())
					throw new IllegalStateException(String.format("Could not read the file '%s'", photoPathTmp));
				
				checkedTrainingSet.get(personId).add(photoFile.getAbsolutePath());
				
			}
		}
		
		train(checkedTrainingSet);
	}

	@Override
	public String predict(String fileName) {
		return predict(fileName, DEFAULT_PREDICTION_CONFIDENCE);
	}

	@Override
	public String predict(String fileName, double confidence) {
		return predict(new InMemoryGrayscaleImage(fileName).getMat(), confidence);
	}
	
	private String predict(Mat faceToPredict, double confidence) {
		log.info("Attempting to recognize a face.");
		
		checkTrainingSet();
		
		// check the confidence boundaries
		if (!(confidence >= 0 && confidence <= 1))
			throw new IllegalArgumentException(String.format("Invalid confidence value. Expected <0..1>, but was %f.", confidence));
		
		// vectorize the test face
		Mat firstFace = trainingFaces[0];
		int faceVectorRows = firstFace.width() * firstFace.height();
		Mat testFaceVector = new Mat(); // our vectorized test face
		Mat testFace = new Mat();
		faceToPredict.convertTo(testFace, CvType.CV_32FC1); // ensure the face is a single-channeled
		testFace.reshape(1, faceVectorRows).convertTo(testFaceVector, CvType.CV_32FC1);
		testFaceVector = testFaceVector.t();
		
		// allocate average test face
		Mat averageTestFace = new Mat();
		
		// allocate eigen vectors matrix for test face
		Mat testFaceEigenVectors = new Mat();
		
		// allocate PCA subspace for projected test face
		Mat projectedTestFace = new Mat();
		
		// compute the average image, eigenvectors and eigenvalues
		Core.PCACompute(testFaceVector, averageTestFace, testFaceEigenVectors);
		
		// project test eigenfaces on the PCA subspace
		Core.PCAProject(testFaceVector, averageFace, eigenVectors, projectedTestFace);
		
		// find the nearest neighbor
		int nearestIndex = -1;
		double leastDistSq = Double.MAX_VALUE;
		
		for (int i = 0; i < trainingFaces.length; i++) {
			
			double distSq = 0;
			
			for (int j = 0; j < eigenValuesCount; j++) {
				double d_i = projectedTestFace.get(0, j)[0]
						- projectedTrainingFaces.get(i, j)[0];
				
				distSq += d_i * d_i;
			}
			
			if (distSq < leastDistSq) {
				leastDistSq = distSq;
				nearestIndex = i;
			}
		}
		
		// Return the confidence level based on the Euclidean distance,
		// so that similar images should give a confidence between 0.5 to 1.0,
		// and very different images should give a confidence between 0.0 to 0.5.
		double calcConfidence = (double) (1.0d - Math.sqrt(leastDistSq / (double) (trainingFaces.length * eigenValuesCount)) / 255.0d);
		
		String recognizedId = null;
		
		if (nearestIndex >= 0) {
			recognizedId = personIds[nearestIndex];
			log.info(String.format("Recognized person '%s' with confidence %f (at <0...1>)", recognizedId, calcConfidence));
			
			if (calcConfidence < confidence) {
				recognizedId = null;
			}
		} else {
			log.info("Failed to recognize the person.");
		}
		
		return recognizedId;
		
	}

	@Override
	public void testPrediction(RecognizerTestScenario scenario) {
		log.info("Recognizer test in progress");
		
		checkTrainingSet();
		
		int totalMatched = 0;
		double successRatio = -1;
		int testSamplesCount = 0;
		
		switch (scenario) {
			case EXISTING_TRAINING_FACES:
				for (int i = 0; i < trainingFaces.length; i++) {
					
					// recognized (predicted) person ID by our recognizer
					String recognizedPersonId = predict(trainingFaces[i], DEFAULT_PREDICTION_CONFIDENCE);
					
					// true person ID based on our training set
					String truePersonId = personIds[i];
					
					// is recognized and true person ID a match?
					boolean match = recognizedPersonId != null 
							&& recognizedPersonId.equals(truePersonId);
					
					String matchStatus = match ? "OK" : "!!! FAIL !!!";
					
					if (match)
						totalMatched++;
					
					log.info(String.format("Training face: "
							+ "#%d "
							+ "of %d. "
							+ "Recognized person: '%s', "
							+ "true person: '%s'. "
							+ "Match: [%s]",
							i+1,
							trainingFaces.length,
							recognizedPersonId,
							truePersonId,
							matchStatus)
						);
				}
				testSamplesCount = trainingFaces.length;
				break;
			case SKIPPED_TRAINING_FACES:
				
				/*
				 *  since this method requires re-training that at this moment 
				 *  requires input xml data containing filepaths to the faces, 
				 *  we will throw an appropriate exception
				 */
				if (inputTrainingData == null) {
					log.error("This test scenario requires re-training, which "
							+ "requires a training set to be passed as argument.");
					throw new IllegalStateException("Re-training required, but no training set passed.");
				}
				
				// save the current training data, we'll modify them, train again and restore them
				Map<String, List<String>> origInputTrainingData = new LinkedHashMap<>();
				
				// allocate holders for test faces
				String[] testPersonIds = new String[personsCount];
				Mat[] testFaces = new Mat[personsCount];
				
				// modify the current training data (select test faces from them and re-train without them)
				log.info("Modifying the training set and re-training the recognizer...");
				int i = 0;
				for (String personId : inputTrainingData.keySet()) {
					if (inputTrainingData.get(personId).size() < 2)
						throw new IllegalStateException("At least two faces of each person are required for this scenario.");
					
					// deep copy of the input data for restoring at the end of the test
					List<String> paths = new LinkedList<String>();
					for (String path : inputTrainingData.get(personId)) {
						paths.add(new String(path));
					}
					origInputTrainingData.put(personId, paths);
					
					// remember the current person's ID
					testPersonIds[i] = personId;
					
					int lastFaceIndex = inputTrainingData.get(personId).size() - 1;
					
					// remember the last face of this person
					testFaces[i] = new InMemoryImage(inputTrainingData.get(personId).get(lastFaceIndex)).getMat();
					
					// remove its last training face from input collection
					inputTrainingData.get(personId).remove(lastFaceIndex);
					
					i++;
				}
				
				// re-train with the modified input data
				log.info("Performing recognizer test...");
				train(inputTrainingData);
				
				// test
				for (i = 0; i < testPersonIds.length; i++) {
					String predictedPersonId = predict(testFaces[i], DEFAULT_PREDICTION_CONFIDENCE);
					
					String truePersonId = testPersonIds[i];
					
					boolean match = predictedPersonId != null
							&& predictedPersonId.equals(truePersonId);
					
					String matchStatus = match ? "OK" : "!!! FAIL !!!";
					
					if (match)
						totalMatched++;
					
					log.info(String.format("Training face: "
							+ "#%d "
							+ "of %d. "
							+ "Recognized person: '%s', "
							+ "true person: '%s'. "
							+ "Match: [%s]",
							i+1,
							testFaces.length,
							predictedPersonId,
							truePersonId,
							matchStatus)
						);
				}
				
				testSamplesCount = testFaces.length;
				
				// re-train with the original training data
				log.info("Re-training the recognizer with the original training set...");
				train(origInputTrainingData);
				break;
		}
		
		// print the test result
		successRatio = 100.0 * totalMatched / testSamplesCount;
		log.info(String.format("Successfuly recognized %d of %d (%f%%) faces.", totalMatched, testSamplesCount, successRatio));
	}
	
	@Override
	public void loadTrainingSet(TrainingSet trainingSet) {
		// TODO 
		
		this.trainingSet = trainingSet;
		
		facesCount = trainingSet.getFacesCount();
		personsCount = trainingSet.getPersonsCount();
		eigenValuesCount = facesCount - 1;
		trainingFaces = trainingSet.getTrainingFaces();
		personIds = trainingSet.getPersonIds();
		eigenVectors = trainingSet.getEigenVectors();
		averageFace = trainingSet.getAverageFace();
		projectedTrainingFaces = trainingSet.getProjectedTrainingFaces();
	}

	//
	
	/**
	 * Parses the XML file containing the training set
	 * @return Map<personId, List<photoFilePath>>
	 */
	private Map<String, List<String>> parseTrainingSetXml(String xmlFilePath) {
		Map<String, List<String>> trainingSet = new LinkedHashMap<String, List<String>>();
		
		// read XML
		File xmlFile = new File(xmlFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("person");
			
			// person
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					 
					Element eElement = (Element) nNode;
					
					String personId = eElement.getAttribute("id");
					
					if (personId == null)
						throw new IllegalStateException("No 'id' attribute found for person at index " + i);
					
					NodeList photos = eElement.getElementsByTagName("photo");
					if (photos.getLength() == 0)
						throw new IllegalStateException(String.format("No photos found for person '%s'", personId));
					
					if (trainingSet.containsKey(personId)) {
						throw new IllegalStateException(String.format("Duplicate person '%s' at [%d]", personId, i));
					}
					
					trainingSet.put(personId, new LinkedList<String>());
					
					// photo
					for (int j = 0; j < photos.getLength(); j++) {
						Node nPhoto = photos.item(j);
						if (nPhoto.getNodeType() == Node.ELEMENT_NODE) {
							Element photo = (Element) nPhoto;
							
							String filePath = photo.getTextContent();
							
							if (filePath == null)
								throw new IllegalStateException(String.format("No filepath found for person '%s', photo [%d]", personId, j));
							
							trainingSet.get(personId).add(filePath);
							
						}
					}
					
				}
			}
			
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("Unable to parse input XML: " + e.getMessage());
			System.exit(1);
		}
		
		return trainingSet;
	}
	
	/**
	 * Performs Principal Component Analysis (PCA) by
	 * finding the average image and eigenfaces in the
	 * the currently loaded training set. 
	 */
	private void doPca() {
		log.info("Performing PCA ...");
		
		eigenValuesCount = facesCount - 1;
		Mat firstFace = trainingFaces[0];
		
		// allocate eigenvectors
		eigenVectors = new Mat();
		
		// allocate eigenvalues vector
		
		// allocate the average image matrix
		averageFace = new Mat();
		
		// allocate face vectors matrix
		int faceVectorRows = firstFace.width() * firstFace.height();
		Mat faceVectors = new Mat(facesCount, faceVectorRows, CvType.CV_32FC1);
		
		// fill the face vectors matrix with face vectors
		for (int i = 0; i < facesCount; i++) {
			Mat faceVector = faceVectors.row(i);
			Mat face = trainingFaces[i];
			// convert face matrix to a face vector
			face.reshape(1,faceVectorRows).convertTo(faceVector, CvType.CV_32FC1);
			// propagate face vector back to the faces vector matrix
			for (int j = 0; j < faceVectorRows; j++) {
				faceVectors.put(i, j, faceVector.get(j, 0));
			}
		}
		
		// allocate PCA subspace matrix
		projectedTrainingFaces = new Mat();
		
		// compute the average image, eigenvectors and eigenvalues
		Core.PCACompute(faceVectors, averageFace, eigenVectors);
		
		// project eigenfaces on the PCA subspace
		Core.PCAProject(faceVectors, averageFace, eigenVectors, projectedTrainingFaces);
		
		// propagate to the trainingset
		trainingSet.setEigenVectors(eigenVectors);
		trainingSet.setAverageFace(averageFace);
		trainingSet.setProjectedTrainingFaces(projectedTrainingFaces);
	}

	@Override
	public TrainingSet getTrainingSet() {
		return trainingSet;
	}

	private boolean checkTrainingSet() {
		return trainingSet != null;
	}
	
}
