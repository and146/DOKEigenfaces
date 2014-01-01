/**
 * 
 */
package and146.projects.eigenfaces.opencv;



import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import and146.projects.eigenfaces.nativelibs.NativeLibrariesUtils;

/**
 * @author neonards
 *
 */
public class OpenCVHelper {
	
	private static Logger log = LoggerFactory.getLogger(OpenCVHelper.class);
	
	public static void loadNativeLibraries() {
		log.debug("Loading OpenCV's native libraries...");
		
		String nativeLibraryName = Core.NATIVE_LIBRARY_NAME + NativeLibrariesUtils.getNativeLibrarySuffix();
		
		log.debug("OpenCV's native library name: " + nativeLibraryName);
		
		NativeLibrariesUtils.loadLibrary(nativeLibraryName);
	}
	
	
}
