/**
 * 
 */
package and146.projects.eigenfaces.opencv;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import and146.projects.eigenfaces.domain.PersistentMat;

/**
 * @author neonards
 *
 */
public class InMemoryImage {
	private String filePath;
	private int flags;
	private Mat mat;
	
	public InMemoryImage(String filePath) {
		this(filePath, Highgui.CV_LOAD_IMAGE_COLOR);
	}
	
	public InMemoryImage(String filePath, int flags) {
		this.filePath = filePath;
		this.flags = flags;
		mat = Highgui.imread(getFilePath(), flags);
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public Mat getMat() {
		return mat;
	}
	
	public int getFlags() {
		return flags;
	}
}
