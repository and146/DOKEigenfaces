/**
 * 
 */
package and146.projects.eigenfaces.nativelibs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import and146.projects.eigenfaces.opencv.OpenCVHelper;

/**
 * @author neonards
 *
 */
public class NativeLibrariesUtils {
	
	private static Logger log = LoggerFactory.getLogger(NativeLibrariesUtils.class);
	
	public static OperatingSystemEnum getOperatingSystem() {
		String osName = System.getProperty("os.name").toLowerCase();
		OperatingSystemEnum os = null;
		
		if (osName.contains("windows"))
			os = OperatingSystemEnum.WINDOWS;
		
		if (osName.contains("linux"))
			os = OperatingSystemEnum.LINUX;
		
		return os;
	}
	
	public static ArchitectureEnum getArchitecture() {
		String archName = System.getProperty("sun.arch.data.model").toLowerCase();
		ArchitectureEnum arch = null;
		
		if (archName.contains("32"))
			arch = ArchitectureEnum.X86;
		
		if (archName.contains("64"))
			arch = ArchitectureEnum.X86_64;
		
		return arch;
	}
	
	public static String getNativeLibrarySuffix() {
		return String.format("_%s_%s", getOperatingSystem().getNativeKey(), getArchitecture().getNativeKey());
	}
	
	public static void loadLibrary(String libraryName) {
		
		OperatingSystemEnum os = getOperatingSystem();
		
		log.debug("Is running from JAR: " + isRunningFromJar());
		
		if (isRunningFromJar()) {
			// when in jar, we need to extract the native libraries to a disk before loading them
			
			String loadedLibraryName =  String.format("%s%s.%s", os.getLibPrefix(), libraryName, os.getLibExtension());
			log.debug(String.format("System-specific library filename: %s", loadedLibraryName));
			
			try {
				 log.debug(String.format("Unpacking native library '%s' from JAR to a local hard drive...", loadedLibraryName));
				 InputStream in = OpenCVHelper.class.getResourceAsStream("/" + loadedLibraryName); // expecting the native libraries to be in the jar's root
				 log.debug(String.format("Native library '%s' found in JAR: %s", loadedLibraryName, in != null));

				 File fileOut = new File(loadedLibraryName);

				 OutputStream out = FileUtils.openOutputStream(fileOut);
				 
				 IOUtils.copy(in, out);
				 in.close();
				 out.close();
			} catch (Exception e) {
				log.error("Unable to load native library: " + loadedLibraryName);
				e.printStackTrace();
			}
			String fullLibraryPath = System.getProperty("user.dir") + os.getFilePathDelimiter() + loadedLibraryName;
			log.debug(String.format("Loading native library '%s' ...", fullLibraryPath));
			System.load(fullLibraryPath);
			log.debug(String.format("Native library '%s' loaded.", fullLibraryPath));
		}
		else {
			log.debug(String.format("Loading native library '%s'...", libraryName));
			System.loadLibrary(libraryName);
			log.debug(String.format("Native library '%s' loaded.", libraryName));
		}
	}
	
	private static boolean isRunningFromJar() {
		return OpenCVHelper.class.getResource("OpenCVHelper.class").toString().startsWith("jar:");
	}
}
